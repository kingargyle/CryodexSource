mvn clean package
jlink --no-header-files\
  --compress=2 \
  --strip-debug \
  --add-modules ALL-MODULE-PATH \
  --output target/JRE
jpackage\
  --type deb \
  --input target/ \
  --main-jar cryodex-4.0.6-SNAPSHOT.jar \
  --icon src/main/resources/cryodex/widget/logo2.jpg \
  --main-class cryodex.Main \
  --runtime-image target/JRE/ \
  --linux-shortcut \
  --name cryodex
mvn clean