(ns kanban.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(defn set-title!
  [board col-index card-index title]
  (swap! board assoc-in [:columns col-index :cards card-index :title] title))

;; application state is a REAGENT atom, not a cljs atom
(defonce app-state (r/atom {:columns [{:title "notes"
                                       :cards [{:title "hello potato"}
                                               {:title "eat ice cream"}]
                                       :editing false}
                                      {:title "holy shit this is awesomesauce"
                                       :cards [{:title "basically magic"
                                                :editing false} ;; use edit flag!
                                               {:title "I am become reagent"}]}]}))

;; reagent cursors!
;; pass atom and path to reagent cursor function.
;; any changes show up in original atom
; (def cards-cursor
;   (r/cursor board [:columns 0 :cards]))


(defn- update-title
  [card-cursor title]
  (swap! card-cursor assoc :title title))

(defn start-editing
  [card-cursor]
  (swap! card-cursor assoc :editing true))

(defn stop-editing
  [card-cursor]
  ; (println (:title @card-cursor))
  (swap! card-cursor assoc :editing false)
  (swap! card-cursor assoc :title (-> (:title @card-cursor) .trim)))

(defn Card
  [card-cursor]
  (let [{:keys [editing title]} @card-cursor]  ;; destructure data in outer let
    (if editing     ;; show input field if edit flag is detected, ingenious :D
      [:div.card.editing
        [:textarea {:type "text"
                    :value title
                    :autoFocus true
                    :on-change #(update-title card-cursor
                                              (.. % -target -value)) ;; e.i. (-> % .-target .-value)
                                                                     ;; % is the on-change event!! :D
                    :on-blur #(stop-editing card-cursor)
                    :on-key-press #(if (= (.-charCode %) 13)          ;; e.i. event.charCode
                                       (stop-editing card-cursor))    ;;  (.-charCode event), (.-charCode %)))
                    :on-key-down  #(if (= (.-keyCode %) 27)           ;; f u browser inconsistency
                                       (stop-editing card-cursor))}]] ;; on-key-down & keyCode for `esc`}]]
      [:div.card
        {:on-click #(start-editing card-cursor)}
        title])))

(defn NewCard
  []
  [:div.new-card
    "+ add new card"])


(defn Column
  [col-cursor]
  (let [{:keys [title cards editing]} @col-cursor] ;; destructuring!!
    [:div.column
      (if editing
        [:input {:type "text" :value title}]
        [:h2 title])
      (for [i (range (count cards))]
        (let [card-cursor (r/cursor col-cursor [:cards i])] ;; [:cards i] rather than [cards i]
          [Card card-cursor]))
      [NewCard]]))

(defn NewColumn
  []
  [:div.new-column
    [:h2 "+ new column"]])

(defn Board
  [board]
  [:div.board
    (for [i (range
              (count (:columns @board)))] ;; get the column index to construct the path!
      (let [col-cursor (r/cursor board [:columns i])]
        [Column col-cursor]))
    [NewColumn]])



(defn render []
  (r/render [Board app-state] (js/document.getElementById "app")))
