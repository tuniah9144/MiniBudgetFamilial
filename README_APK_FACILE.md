# Mini Budget Familial V2 — obtenir l'APK très facilement

## Option A — la plus facile (sans Android Studio)

1. Créer un compte gratuit sur GitHub.
2. Créer un nouveau dépôt, par exemple `MiniBudgetFamilial`.
3. Envoyer tout le contenu de ce dossier dans le dépôt (bouton **Add file > Upload files**).
4. Ouvrir l'onglet **Actions** puis le workflow **Construire APK Mini Budget Familial**.
5. Cliquer sur **Run workflow** si nécessaire.
6. Quand le travail est terminé avec une coche verte, ouvrir le résultat du workflow et télécharger **MiniBudgetFamilial-APK**.
7. Décompresser le fichier téléchargé : il contient `app-debug.apk`.
8. Envoyer `app-debug.apk` sur le téléphone Android, l'ouvrir et choisir **Installer**.

Aucun Android Studio n'est nécessaire sur le PC avec cette méthode. GitHub construit l'APK dans le cloud.

## Option B — Android Studio

1. Décompresser le projet.
2. Ouvrir `MiniBudgetFamilial_V2` dans Android Studio.
3. Laisser Android Studio télécharger/synchroniser les composants nécessaires.
4. Choisir **Build > Build APK(s)**.
5. L'APK se trouve dans `app/build/outputs/apk/debug/app-debug.apk`.

## Configuration

- Application ID : `mg.budget.familial`
- Version : 2.0
- Android minimum : 8.0 (API 26)
- Cible : Android 15 (API 35)
- Java : 17
- Gradle utilisé pour le build automatique : 8.10.2

## Installation sur le téléphone

Pour une installation manuelle, Android peut demander l'autorisation d'installer des applications provenant de la source utilisée pour ouvrir le fichier APK. Active cette autorisation uniquement pour cette installation si nécessaire.

## Important

L'APK `debug` est destiné à un usage personnel/test. Pour publier l'application sur Google Play, il faudra ensuite créer une version signée (`release`) avec une clé de signature.
