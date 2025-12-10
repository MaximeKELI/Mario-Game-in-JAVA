# Super Mario Clone

Un jeu de plateforme 2D avancé inspiré de Mario, développé en Java avec libGDX et Box2D. Ce projet implémente un moteur de jeu complet avec physique, collisions, animations et gestion des entrées utilisateur.

## 🚀 Prérequis

- Java 11 ou supérieur
- Gradle 7.0+
- LibGDX 1.11.0

## 🎮 Comment exécuter

1. Clonez ce dépôt :
   ```bash
   git clone [URL_DU_REPO]
   cd jeu_java
   ```

2. Lancez le jeu :
   ```bash
   # Sur Linux/Mac
   ./gradlew desktop:run
   
   # Sur Windows
   gradlew.bat desktop:run
   ```

3. Ou générez un fichier JAR exécutable :
   ```bash
   ./gradlew desktop:dist
   # Le fichier sera généré dans desktop/build/libs/
   ```

## 🎮 Contrôles

- **Flèche gauche/droite** : Se déplacer
- **Flèche haut/Espace/Z** : Sauter
- **Maj gauche/droite** : Courir
- **Flèche bas** : S'accroupir
- **Échap** : Quitter le jeu
- **F1** : Basculer le mode debug

## 🎯 Fonctionnalités implémentées

### 🎮 Mécaniques de jeu
- Moteur physique Box2D avancé avec gestion précise des collisions
- Déplacement fluide avec accélération et frottements
- Saut avec gravité réaliste et détection du sol
- Système de course avec accroupissement
- Gestion des plateformes et des obstacles
- Système de score et de vies
- Chronomètre de partie

### 🖥️ Technique
- Architecture modulaire et orientée objet
- Gestion des entrées utilisateur avancée
- Système de caméra dynamique avec suivi fluide
- Gestion des états du joueur (marche, course, saut, chute, accroupi)
- Système de débogage intégré (appuyez sur F1)
- HUD informatif (score, vies, temps, pièces)

### 🎨 Graphismes
- Animations fluides pour tous les états du personnage
- Arrière-plan défilant
- Effets visuels (clignotement lors des dégâts)
- Système de spritesheets pour les animations

## 📁 Structure du projet

```
core/
├── assets/                  # Ressources du jeu
│   ├── player/             # Sprites et animations du joueur
│   ├── levels/             # Fichiers de niveau
│   ├── sounds/             # Effets sonores et musique
│   └── ui/                 # Éléments d'interface utilisateur
├── src/com/mariogame/core/ # Code source principal
│   ├── entities/           # Entités du jeu
│   ├── screens/            # Écrans du jeu (menu, jeu, game over)
│   ├── utils/              # Utilitaires
│   ├── Hud.java            # Gestion de l'interface utilisateur
│   ├── MarioGame.java      # Classe principale du jeu
│   └── Player.java         # Logique du joueur
desktop/                    # Point d'entrée pour la version desktop
```

## 🚧 Améliorations futures

### 🎮 Gameplay
- [ ] Ajout d'ennemis avec IA simple
- [ ] Système de pouvoir (fleur de feu, champignon, étoile)
- [ ] Plateformes mobiles et objets interactifs
- [ ] Système de sauvegarde
- [ ] Menu principal et écrans de transition

### 🎨 Contenu
- [ ] Plus de niveaux avec thèmes variés
- [ ] Effets sonores et musique de fond
- [ ] Plus d'animations et d'effets visuels
- [ ] Système de particules pour les effets spéciaux

### 🛠 Technique
- [ ] Optimisation des performances
- [ ] Gestion de la mémoire améliorée
- [ ] Support des contrôles tactiles
- [ ] Internationalisation (multi-langues)

## 📝 Notes pour les contributeurs

1. Suivez les conventions de code existantes
2. Documentez votre code avec des commentaires clairs
3. Testez vos modifications avant de soumettre une pull request
4. Utilisez des messages de commit descriptifs

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.
# Mario-Game-in-JAVA
