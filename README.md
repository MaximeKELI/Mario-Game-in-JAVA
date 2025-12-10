# 🍄 Super Mario Clone

[![Java CI with Gradle](https://github.com/MaximeKELI/CS50P-HARVARD/actions/workflows/ci.yml/badge.svg)](https://github.com/MaximeKELI/CS50P-HARVARD/actions/workflows/ci.yml)
[![Code Coverage](https://codecov.io/gh/MaximeKELI/CS50P-HARVARD/branch/main/graph/badge.svg)](https://codecov.io/gh/MaximeKELI/CS50P-HARVARD)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> 🌟 *"It's-a me, Mario!"* - Un clone fidèle du célèbre jeu de plateforme avec une touche moderne

Un jeu de plateforme 2D avancé inspiré de Mario, développé en Java avec libGDX et Box2D. Ce projet implémente un moteur de jeu complet avec physique, collisions, animations et gestion des entrées utilisateur.

## 🚀 Prérequis

- Java 11 ou supérieur
- Gradle 7.0+
- LibGDX 1.11.0

## 💻 Installation et exécution

1. **Cloner le dépôt** :
   ```bash
   git clone https://github.com/MaximeKELI/CS50P-HARVARD.git
   cd CS50P-HARVARD
   ```

2. **Lancer le jeu** :
   ```bash
   # Sur Linux/Mac
   ./gradlew desktop:run
   
   # Sur Windows
   gradlew.bat desktop:run
   ```

3. **Générer un exécutable** :
   ```bash
   ./gradlew desktop:dist
   # Le fichier JAR sera disponible dans desktop/build/libs/
   ```

## 🎮 Contrôles

| Touche | Action |
|--------|--------|
| ← → | Se déplacer |
| ↑ / Espace / Z | Sauter |
| Maj gauche/droite | Courir |
| ↓ | S'accroupir |
| Échap | Quitter le jeu |
| F1 | Activer le mode debug |

## ✨ Fonctionnalités

### 🎮 Mécaniques de jeu
- 🏃‍♂️ Moteur physique Box2D avancé avec gestion précise des collisions
- 🎯 Déplacement fluide avec accélération et frottements
- ⚡ Saut avec gravité réaliste et détection du sol
- 🏃 Système de course avec accroupissement
- 🧱 Gestion des plateformes et des obstacles
- 🏆 Système de score et de vies
- ⏱️ Chronomètre de partie

### 🖥️ Architecture technique
- 🏗️ Architecture modulaire et orientée objet
- 🎛️ Gestion avancée des entrées utilisateur
- 🎥 Caméra dynamique avec suivi fluide
- 🎮 Gestion des états du joueur (marche, course, saut, chute, accroupi)
- 🔍 Système de débogage intégré (touche F1)
- 📊 HUD informatif (score, vies, temps, pièces)

### 🎨 Graphismes et sons
- 🎭 Animations fluides pour tous les états du personnage
- 🌄 Arrière-plan défilant
- ✨ Effets visuels (clignotement lors des dégâts)
- 🖼️ Système de spritesheets pour les animations
- 🎵 Musique et effets sonores immersifs

## 🏗️ Structure du projet

```
src/
├── main/
│   ├── java/com/mariogame/
│   │   ├── core/            # Logique principale du jeu
│   │   │   ├── entities/    # Personnages et objets du jeu
│   │   │   ├── managers/    # Gestionnaires (son, collisions, etc.)
│   │   │   ├── screens/     # Écrans (menu, jeu, game over)
│   │   │   ├── Hud.java     # Interface utilisateur
│   │   │   └── MarioGame.java # Classe principale
│   └── resources/           # Assets (images, sons, niveaux)
│       ├── player/          # Sprites et animations du joueur
│       ├── levels/          # Fichiers de niveau
│       └── sounds/          # Effets sonores et musique
desktop/                    # Point d'entrée pour la version desktop
```

## 🚀 Améliorations futures

### 🎮 Gameplay
- [ ] Ajout d'ennemis avec IA simple
- [ ] Système de pouvoir (fleur de feu, champignon, étoile)
- [ ] Plateformes mobiles et objets interactifs
- [ ] Système de sauvegarde
- [ ] Menu principal et écrans de transition
- [ ] Niveaux additionnels avec thèmes variés
- [ ] Mode multijoueur local
- [ ] Plus d'animations et d'effets visuels
- [ ] Système de particules pour les effets spéciaux

### 🛠️ Technique
- [ ] Amélioration des performances
- [ ] Meilleure gestion de la mémoire
- [ ] Tests unitaires supplémentaires
- [ ] Documentation du code
- [ ] Support des contrôles tactiles
- [ ] Internationalisation (multi-langues)

## 🤝 Contribution

Les contributions sont les bienvenues ! Voici comment contribuer :

1. Forkez le projet
2. Créez une branche pour votre fonctionnalité (`git checkout -b feature/nouvelle-fonctionnalite`)
3. Committez vos changements (`git commit -m 'Ajout d'une nouvelle fonctionnalité'`)
4. Poussez vers la branche (`git push origin feature/nouvelle-fonctionnalite`)
5. Ouvrez une Pull Request

## 📜 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

<div align="center">
  Fait avec ❤️ par MaximeKELI | Inspiré du célèbre jeu Mario de Nintendo®
</div>

## 📝 Notes pour les contributeurs

1. Suivez les conventions de code existantes
2. Documentez votre code avec des commentaires clairs
3. Testez vos modifications avant de soumettre une pull request
4. Utilisez des messages de commit descriptifs
