#!/bin/bash

# Définir le répertoire de base
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Créer le répertoire de sortie s'il n'existe pas
mkdir -p "$BASE_DIR/bin"

# Trouver tous les fichiers .java
find "$BASE_DIR" -name "*.java" > sources.txt

# Compiler le projet
echo "Compilation en cours..."
javac -d "$BASE_DIR/bin" \
     -cp ":$BASE_DIR/lib/*" \
     @sources.txt

# Vérifier si la compilation a réussi
if [ $? -eq 0 ]; then
    echo "Compilation réussie !"
    
    # Exécuter le jeu
echo "Lancement du jeu..."
java -cp ":$BASE_DIR/bin:$BASE_DIR/lib/*" \
     -Djava.library.path="$BASE_DIR/lib" \
     com.mariogame.desktop.DesktopLauncher
else
    echo "Erreur lors de la compilation"
    exit 1
fi

# Nettoyer
rm -f sources.txt
