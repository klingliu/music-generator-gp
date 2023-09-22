(ns insane-noises.core)

;;make a diverse "song" with different tones
(defn random [min max]
  (+ (rand-int (- max min)) min))

;(def foo [b] (synth (out 0 (pan2 (sin-osc b))))) ;just creates a synth
;
;(defsynth my-sin [freq 440 sustain 1]
;          (out 0 (pan2 (sin-osc freq))))
;; like fn and defn, always need a value assigned or uses default
;
;(defn play [note duration]
;  (let [time (now)]
;    (if (= note -1)
;      (at (time * duration 2000) (my-sin 0))
;      (at (time * duration 2000) (piano note)))))



;;;Our ingredients
;randomly generated ~30 individuals
;creates a gene with freq + duration (actual length of notes)
(defn ram [] (vector (random 36 84) (first (shuffle [1/2 1 2 4]))))

;;;;;;;Error Function Helpers
(defn absolute [n]
  (if (< n 0)
    (- n)
    n))

(defn pair-error [[[p1 d1] [p2 d2]]]
  (if (or (= p1 (- 12 p2))                                  ;;octave
          (= p1 (+ 12 p2))
          (= p1 p2))
    (if (>= (rand) 0.5)                                     ;;50%
      200
      0)
    0)
  (if (= p1 (+ 6 p2))                                       ;;tritone
    (if (<= (rand) 0.6)                                     ;;30%
      200
      0)
    0)
  (if (> (- p1 p2) 7)                                       ;; over a fifth
    (if (<= (rand) 0.9)                                     ;;10%
      200
      0)
    0))

;;;;Error Function

(defn error [genome]
  (+ (reduce + (map pair-error (partition 2 1 genome)))
     (if (or (<= (count genome) 30)
             (> (count genome) 100))
       10000000
       (/ 10000.0 (- (count genome) 30)))))

;; the error will tend to be large, bc as long as the count is in
;; between 30-100 the error won't reach 0
;;;;;;;


(defn new-individual []
  "Returns a new, random individual in the context of test-pairs."
  (let [genome (vec (repeatedly 65 ram))]
    {:genome genome
     :error  (error genome)}))

(defn best [individuals]
  "Returns the best of the given individuals."
  (reduce (fn [i1 i2]
            ;(print i1, i2)
            (if (< (:error i1) (:error i2))
              i1
              i2))
          individuals))

(defn lexicase-selection
  "Selects an individual from the population using lexicase selection."
  [pop]
  (loop [survivors (map rand-nth (vals (group-by :errors pop)))
         cases (shuffle (range (count (:errors (first pop)))))]
    (if (or (empty? cases)
            (empty? (rest survivors)))
      (rand-nth survivors)
      (let [min-err-for-case (apply min (map #(nth % (first cases))
                                             (map :errors survivors)))]
        (recur (filter #(= (nth (:errors %) (first cases)) min-err-for-case)
                       survivors)
               (rest cases))))))

(defn mutate [individual]
  "Returns a possibly-mutated copy of genome."
  (let [genome (:genome individual)
        with-additions (flatten (for [g genome]
                                  (if (< (rand) 0.3)
                                    (shuffle (list g (ram))) ;not using ingredients list
                                    g)))
        with-deletions (flatten (for [g genome]
                                  (if (< (rand) 0.3)
                                    ()
                                    g)))]
    (if (< (rand) 0.5)
      (partition 2 with-additions)
      (partition 2 with-deletions))))

(defn crossover                                             ;taken from Propeller
  "Crosses over two individuals using uniform crossover. Pads shorter one."
  [genome1 genome2]
  (let [plushy-a (:genome genome1)
        plushy-b (:genome genome2)
        shorter (min-key count plushy-a plushy-b)
        longer (if (= shorter plushy-a)
                 plushy-b
                 plushy-a)
        length-diff (- (count longer) (count shorter))
        shorter-padded (concat shorter (repeat length-diff :crossover-padding))]
    (remove #(= % :crossover-padding)
            (map #(if (< (rand) 0.5) %1 %2)
                 shorter-padded
                 longer))))

(defn make-child [population]
  "Returns a new, evaluated child, produced by mutating the result
  of crossing over parents that are selected from the given population."
  (if (< (rand) 1.0)
    (let [new-genome (mutate (lexicase-selection population))] ;breaks when making a child
      {:genome new-genome
       :error  (error new-genome)})
    (let [new-genome (crossover (lexicase-selection population)
                                (lexicase-selection population))]
      {:genome new-genome
       :error  (error new-genome)})))

(defn report [generation population]
  "Prints a report on the status of the population at the given generation."
  (let [current-best (best population)]
    (println {:generation   generation
              :best-error   (:error current-best)
              :diversity    (float (/ (count (distinct population))
                                      (count population)))
              :average-size (float (/ (->> population
                                           (map :genome)
                                           (map count)
                                           (reduce +))
                                      (count population)))
              :best-genome  (:genome current-best)})))

(defn gp [population-size generations]
  "Runs genetic programming to solve, or approximately solve, a floating-point
  symbolic regression problem in the context of the given population-size,
  number of generations to run, and test-pairs."
  (loop [population (repeatedly population-size
                                #(new-individual))
         generation 0]
    (report generation population)
    (if (or (< (:error (best population)) 0.1)
            (>= generation generations))
      (best population)
      (recur (conj (repeatedly population-size
                               #(make-child population)))
             (inc generation)))
    ))
