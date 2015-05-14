(ns jafama-perf-test.core
  (:require [criterium.core :as criterium :refer [with-progress-reporting bench quick-bench]]
            [primitive-math])
  (:import net.jafama.FastMath))

(primitive-math/use-primitive-operators)

(defn random-value
  "Returns a random double value between min-value and max-value"
  ^double [^double min-value ^double max-value]
  (let [dist (- max-value min-value)]
    (+ min-value ^double (rand dist))))

(defn abs-perc-diff
  "Returns the absolute value of the difference between v1 and v2 as a percentage of v1."
  ^double [^double v1 ^double v2]
  (* (/ (Math/abs (- v1 v2)) v1 ) 100.0))

(def num-args->math-fn-format-str
  {1 "#(%s/%s ^double %%)"
   2 "#(%s/%s ^double %%1 ^double %%2)"})

(defn make-math-fn
  "Takes a symbol representina class, a symbol of the method to call on the class, and the number of 
  arguments. Returns a function taking that number of arguments and delegating to the class method
  given."
  [class-symbol fn-symbol num-args]
  (eval (read-string 
          (format 
            (num-args->math-fn-format-str num-args) 
            (name class-symbol) 
            (name fn-symbol)))))

(defn measure-accuracy
  "Measures the accuracy of the given function on FastMath by comparing it to the result of Math.
  Takes a symbol of the FastMath method to call and an options map."
  [fn-symbol {:keys [minv maxv num-args num-samples]}]
  (let [math-fn (make-math-fn 'Math fn-symbol num-args)
        fast-math-fn (make-math-fn 'net.jafama.FastMath fn-symbol num-args)]
    (reduce (fn [^double fm-max-diff  _]
              
              (when (Double/isNaN fm-max-diff)
                (println fn-symbol "generated NAN")
                (println fm-max-diff)
                (throw (Exception. "NAN")))
              
              (let [values (for [_ (range num-args)] (random-value minv maxv))
                    math-v (apply math-fn values)
                    fast-math-v (apply fast-math-fn values)]
                
                (when (or (Double/isNaN math-v)
                          (Double/isNaN fast-math-v))
                  (println fn-symbol "generated NAN")
                  (println (pr-str values))
                  (throw (Exception. "NAN")))
                
                (max fm-max-diff (abs-perc-diff math-v fast-math-v))))
            0.0
            (range num-samples))))

(def num-args->math-bench-fn-format-str
  {1 "(fn [^double v] (criterium.core/bench (%s/%s v)))"
   2 "(fn [^double v1 ^double v2] (criterium.core/bench (%s/%s v1 v2)))"})

(defn make-math-bench-fn
  "Takes a symbol representina class, a symbol of the method to call on the class, and the number of 
  arguments. Returns a function taking that number of arguments that will use criterium to measure
  the performance of the function."
  [class-symbol fn-symbol num-args]
  (eval (read-string 
          (format 
            (num-args->math-bench-fn-format-str num-args) 
            (name class-symbol) 
            (name fn-symbol)))))

(defn measure-performance
  "Executes the function using criterium against Java Math and FastMath to compare the performance.
  Takes a symbol representing the method to call along with an options map."
  [fn-symbol {:keys [minv maxv num-args]}]
  (let [math-fn (make-math-bench-fn 'Math fn-symbol num-args)
        fast-math-fn (make-math-bench-fn 'net.jafama.FastMath fn-symbol num-args)]
    
    (println fn-symbol)
    (case (int num-args)
      
      1
      (let [value (random-value minv maxv)]
        (println "FastMath")
        (fast-math-fn value)
        (println "Math")
        (math-fn value))
      2
      (let [value1 (random-value minv maxv)
            value2 (random-value minv maxv)]
        (println "FastMath")
        (fast-math-fn value1 value2)
        (println "Math")
        (math-fn value1 value2)))))


(def TWO_PI (* 2.0 Math/PI))
(def NEG_TWO_PI (* -2.0 Math/PI))

(def functions-to-measure
  "Defines a map of methods to call on FastMath and option maps for calling them. Each option map
  defines a range of values, the number of arguments, and the number of samples to use use
  when measuring accuracy."
  (let [num-samples 100000
        normal-opts {:minv -10000.0
                     :maxv 10000.0
                     :num-args 1
                     :num-samples num-samples}
        pos-small-opts {:minv 0
                        :maxv 100
                        :num-args 1
                        :num-samples num-samples}
        trig-opts {:minv NEG_TWO_PI 
                   :maxv TWO_PI
                   :num-args 1
                   :num-samples num-samples}
        log-opts {:minv 0.0001
                  :maxv 100000.0
                  :num-args 1
                  :num-samples num-samples}
        arc-trig-opts {:minv -1.0
                       :maxv 1.0
                       :num-args 1
                       :num-samples num-samples}
        normal-two-arg-opts {:minv -10000.0
                             :maxv 10000.0
                             :num-args 2
                             :num-samples num-samples}]
    {
     'sin trig-opts
     'sinh trig-opts
     'cos trig-opts
     'cosh trig-opts
     'tan trig-opts
     'tanh trig-opts
     'asin arc-trig-opts
     'acos arc-trig-opts
     'atan arc-trig-opts
     'atan2 normal-two-arg-opts
     'hypot normal-two-arg-opts
     
     'exp pos-small-opts
     'expm1 pos-small-opts
     
     'log log-opts
     'log10 log-opts
     'log1p log-opts
     
     'sqrt log-opts
     'cbrt trig-opts
     'floor normal-opts
     'ceil normal-opts
     'round normal-opts
     'toRadians {:minv -360.0
                 :maxv 360.0
                 :num-args 1
                 :num-samples num-samples}
     'toDegrees trig-opts
     
     'nextUp normal-opts
     'nextAfter normal-two-arg-opts
     
     ;; Doesn't fit easily into our test. It takes double, int
     ;'scalb normal-two-arg-opts
     
     'pow {:minv 0.0001
           :maxv 100.0
           :num-args 2
           :num-samples num-samples}
     
     }))

(defn evaluate-function-accuracy
  "Evaluates the accuracy of all the defined functions. Prints results to standard out."
  []
  (println "Results are printed as the worst case percentage difference between the FastMath and Math results.")
  (doseq [[fn-symbol options] functions-to-measure]
    (let [fast-math-accuracy (measure-accuracy fn-symbol options)]
      (println fn-symbol fast-math-accuracy))))

(defn evaluate-performance
  "Evaluates the performance of all the defined functions. Prints results to standard out."
  []
  (println "Prints out Criterium results for each function. Takes a long time to run.")
  (doseq [[fn-symbol options] functions-to-measure]
    (println "-------------------------------------------------------------")
    (measure-performance fn-symbol options)))

