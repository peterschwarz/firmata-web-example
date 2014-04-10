(defproject firmata-web-example "0.1.0-SNAPSHOT"
  :description "An example connecting an ardiuno with an analog sensor to the web."
  :url "http://example.com/FIXME"

  :source-paths ["src" "src/clj"]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring "1.2.1"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [http-kit "2.1.16"]
                 [clj-time "0.6.0"]
                 [com.taoensso/timbre "3.0.1"]
                 [org.clojure/clojurescript "0.0-2014"
                  :exclusions [org.apache.ant/ant]]
                 [domina "1.0.2"]
                 [clj-firmata "1.0.0"]]

  :plugins [[lein-cljsbuild "1.0.2"]]

  :main firmata-web.handler

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/cljs.js"
                           :optimizations :whitespace
                           :pretty-print true}}]}
  )
