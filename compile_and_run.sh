#!/bin/bash

# Créer le dossier de sortie
mkdir -p bin

# Trouver tous les fichiers .java
find . -name "*.java" > sources.txt

# Compiler
javac -d bin @sources.txt -cp "./lib/*"

# Vérifier si la compilation a réussi
if [ $? -eq 0 ]; then
    echo "Compilation réussie !"
    
    # Créer le fichier manifeste
echo "Manifest-Version: 1.0" > manifest.txt
echo "Main-Class: com.mariogame.desktop.DesktopLauncher" >> manifest.txt
echo "Class-Path: lib/*" >> manifest.txt

# Créer le JAR
jar cvfm mario-game.jar manifest.txt -C bin .

# Exécuter le jeu
java -jar mario-game.jar
else
    echo "Erreur lors de la compilation"
    exit 1
fi

# Nettoyer
rm -f sources.txt manifest.txt
