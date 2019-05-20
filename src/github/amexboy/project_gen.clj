(ns github.amexboy.project-gen
  (:require [clojure.string :as st]
            [clojure.java.io :as io]))

(defn maven-root? [dir]
  "Checks if the dir contains a maven.pom file"
  (some (partial = "pom.xml") (.list dir))
  )
(defn gradle-root? [dir]
  "Checks if the dir contains a maven.pom file"
  (some (partial = "build.gradle") (.list dir))
  )
(defn liengen-root? [dir]
  "Checks if the dir contains a maven.pom file"
  (some (partial = "project.clj") (.list dir))
  )

(defn gen-maven-iml [path name]
  "Generates an IDEA module for the path with the given name"
  (let [template "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
           <module org.jetbrains.idea.maven.project.MavenProjectsManager.isMavenModule=\"true\" type=\"JAVA_MODULE\" version=\"4\">
           <component name=\"ExternalSystem\" externalSystem=\"Maven\" />
        </module>"]
    (spit (str path "/" name ".iml") template))
  )

(defn gen-gradle-iml [path name]
  "Generates an IDEA module for the path with the given name"
  (let [template "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
           <module org.jetbrains.idea.gradle.project.GradlenProjectsManager.isGradleModule=\"true\" type=\"JAVA_MODULE\" version=\"4\">
           <component name=\"ExternalSystem\" externalSystem=\"Maven\" />
        </module>"]
    (spit (str path "/" name ".iml") template))
  )

(defn gen-liengen-iml [path name]
  "Generates an IDEA module for the path with the given name"
  (let [
        template "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
                     <module cursive.leiningen.project.LeiningenProjectsManager.displayName=\"" name "\"
                             cursive.leiningen.project.LeiningenProjectsManager.isLeinModule=\"true\"
                             type=\"JAVA_MODULE\" version=\"4\">
                     <component name=\"ExternalSystem\" externalSystem=\"Maven\" />
                  </module>"]
    (spit (str path "/" name ".iml") template))
  )

(defn gradle-entry [module] 
  (str "
  <component name=\"GradleSettings\">
    <option name=\"linkedExternalProjectsSettings\">
      <GradleProjectSettings>
        <option name=\"distributionType\" value=\"DEFAULT_WRAPPED\" />
        <option name=\"externalProjectPath\" value=\"$PROJECT_DIR$/" module "/\" />
        <option name=\"useQualifiedModuleNames\" value=\"true\" />
      </GradleProjectSettings>
    </option>
 </component>
  ")
  )

(defn module-entries [root module]
  "Returns component entries for git, pom (gradle soon) and git component lists"
  (let [entries {
                 :iml (str "<module filepath=\"file://$PROJECT_DIR$/" module "/" module ".iml\"")
                 :git (str "<mapping directory=\"$PROJECT_DIR$/" module "\" vcs=\"Git\" />")
                 }
        module-file (io/file root module)]
    (cond
      (maven-root? module-file) (assoc entries :pom (str "<option value=\"$PROJECT_DIR$/" module "/pom.xml\" />"))
      (gradle-root? module-file) (assoc entries :gradle (gradle-entry module))
      ))
  )

(defn reducer [r v]
  (reduce #(assoc %1 %2 (cons (%2 r) (%2 v)))
          r (keys v)))

(defn module-components [root modules]
  "Returns iml git and pom (gradle soon) component entries grouped by type"
  (reduce reducer
          {:iml ()
           :git ()
           :pom ()
           }
          (map #(module-entries root %) modules)))


(defn project-ipr [module-components]
  "Takes a list of modules and generates an Intellij project file"
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <project version=\"4\">
          <component name=\"InspectionProjectProfileManager\">
            <profile version=\"1.0\">
              <option name=\"myName\" value=\"Project Default\" />
            </profile>
            <version value=\"1.0\" />
          </component>
          <component name=\"MavenProjectsManager\">
            <option name=\"originalFiles\">
              <list>
                " (st/join "\n\t\t\t\t" (:pom module-components)) "
              </list>
            </option>
          </component>

          <component name=\"ProjectModuleManager\">
            <modules>
                " (st/join "\n\t\t\t\t" (:iml module-components)) "
            </modules>
          </component>
          <component name=\"ProjectRootManager\" version=\"2\" languageLevel=\"JDK_1_8\" default=\"false\" project-jdk-name=\"1.8\" project-jdk-type=\"JavaSDK\">
            <output url=\"file://$PROJECT_DIR$/out\" />
          </component>
          <component name=\"VcsDirectoryMappings\">
            " (st/join "\n\t\t\t" (:git module-components)) "
          </component>
          <component name=\"ChangeListManager\">
            <list default=\"true\" id=\"6ad0e189-9fce-4c3a-92b7-a19a9f7997b1\" name=\"${ticketId}\" comment=\"${ticketId}\" />
          </component>
        </project>")
  )

(defn gen-ipr [root name modules config]
  "Writes generated IPR file"
  (let [dir (io/file root (str name ".ipr"))]
    (io/make-parents dir)
    (spit dir (project-ipr (module-components root modules))))
  )
