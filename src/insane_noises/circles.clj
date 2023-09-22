(ns insane-noises.circles
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [insane-noises.core :as core]
            ;[overtone.live :as o]
            ;[overtone.inst.piano]
            ))

(require '[dynne.sampled-sound :as d])

(defn midi-to-hz
  [m]
  (if (< m 21)
    0
    (* 440 (Math/pow 2
                     (-> m (- 69) (/ 12.0))))))

(defn scale [x min max a b]
  (+ a
     (-> (- b a)
         (* (- x min))
         (/ (- max min)))))

(defn scale-through [coll a b]
  (let [min (apply min coll)
        max (apply max coll)]
    (map #(scale % min max a b) coll)))



; count-occurrences function was copied from
; https://stackoverflow.com/questions/25614540/clojure-how-to-count-occurrences-in-a-list/25616080#25616080
(defn count-occurrences [obj vec]
  (->> vec
       (filter #{obj})
       count))

(defn setup []

  (q/color-mode :hsb)

  ;(let [results [60 60 67 67 69 69 67 65 65 64 64 62 62 60]
  ;      twinkle [60 60 67 67 69 69 67 65 65 64 64 62 62 60]
  ;      scaled-twinkle (scale-through twinkle 200.0 800.0)
  ;      durations [1.0 1.0 1.0 1.0 1.0 1.0 2.0 1.0 1.0 1.0 1.0 1.0 1.0 2.0]
  ;      colors (map #(Math/floor %)
  ;                  (scale-through twinkle 0 185))]

  (let [results (core/gp 50 100)
        genome (:genome results)
        twinkle (vec (map first genome))
        scaled-twinkle (scale-through twinkle 200.0 800.0)
        durations (vec (map second genome))
        colors (map #(Math/floor %)
                    (scale-through twinkle 0 185))]

    (clojure.pprint/pprint (assoc results :length (count (:genome results))))

    {:song                 twinkle
     :scaled-song          scaled-twinkle
     :all-colors           colors

     :note                 (first twinkle)
     :duration             (first durations)
     :radius               (first scaled-twinkle)
     :color                (first colors)
     :brightness           75
     :brightness-increment (Math/floor (/ 180 (reduce max (vals (frequencies twinkle)))))
     :frames               0

     :prev-notes           [(first scaled-twinkle)]
     :prev-colors          [(first colors)]
     :prev-brightnesses    [55]
     :prev-count           1

     :next-real-notes      (next twinkle)
     :next-notes           (next scaled-twinkle)
     :next-durs            (next durations)
     :next-colors          (next colors)

     :last                 false
     :player               (d/play (d/sinusoid 50.0 (midi-to-hz (first twinkle))))}))

(defn update-state [state]

  (if (< (Math/abs
           (- (* 2.5 (:duration state))
              (:frames state)))
         0.1)

    (if (nil? (first (:next-real-notes state)))

      (assoc state :last true)

      (let []
        (d/stop (:player state))
        {:song                 (:song state)
         :scaled-song          (:scaled-song state)
         :all-colors           (:all-colors state)

         :note                 (first (:next-real-notes state))
         :duration             (first (:next-durs state))
         :radius               (first (:next-notes state))
         :color                (first (:next-colors state))
         :frames               0

         :brightness-increment (:brightness-increment state)
         :brightness           (+ 55 (* (:brightness-increment state)
                                        (count-occurrences (first (:next-notes state))
                                                           (:prev-notes state))))
         :prev-brightnesses    (conj (:prev-brightnesses state)
                                     (+ 55 (* (:brightness-increment state)
                                              (count-occurrences (first (:next-notes state))
                                                                 (:prev-notes state)))))

         :prev-notes           (conj (:prev-notes state)
                                     (first (:next-notes state)))
         :prev-colors          (conj (:prev-colors state)
                                     (first (:next-colors state)))
         :prev-count           (+ 1 (:prev-count state))

         :next-real-notes      (next (:next-real-notes state))
         :next-notes           (next (:next-notes state))
         :next-durs            (next (:next-durs state))
         :next-colors          (next (:next-colors state))

         :last                 false
         :player               (d/play (d/sinusoid 50.0
                                                   (midi-to-hz (first (:next-real-notes state)))))}
        ))

    (assoc state :radius (- (:radius state) 1.0)
                 :frames (+ 0.1 (:frames state)))))

(defn draw-state
  [state]
  (q/background 0)
  (q/no-fill)

  (dotimes [n (:prev-count state)]
    (let [old-notes (:prev-notes state)
          old-colors (:prev-colors state)
          old-brightnesses (:prev-brightnesses state)
          r (nth old-notes n)
          color (nth old-colors n)
          brightness (nth old-brightnesses n)]
      (q/stroke color 255 brightness)
      (q/stroke-weight 2)
      (q/ellipse 750 450 r r)))

  (if (:last state)
    (let []
      (d/stop (:player state)))
    (let [r (:radius state)]
      (q/stroke 255)
      (q/stroke-weight 3)
      (q/ellipse 750 450 r r))))

; leave behind an imprint of the note, and make the color brighter
; every time it is hit again and have the color hue depend on the frequency

(q/defsketch mysketch
             :host "host"
             :size [1520 920]                               ;1520
             :setup setup
             :update update-state
             :draw draw-state
             :middleware [m/fun-mode])