(defproject jafama-perf-test "0.1.0-SNAPSHOT"
  :description "Provides a basic performance and accuracy test of the Jafama library."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  ;; Local repository where jafama is installed. (See README.)
  :repositories {"local" ~(str (.toURI (java.io.File. "maven_repository")))}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [jafama/jafama "2.1"]
                 [criterium "0.4.3"]
                 [primitive-math "0.1.3"]]
  
  :global-vars {*warn-on-reflection* true}
  
  :jvm-opts ^:replace ["-Djafama.fastlog=true" "-Djafama.fastsqrt=true"]
  
  :profiles 
  {:dev {:source-paths ["dev" "src" "test"]
         :dependencies [[org.clojure/tools.namespace "0.2.10"]]}})