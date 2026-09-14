# Mini Budget Familial Android — V2

Version 2 du projet :
- stockage permanent local des dépenses (SharedPreferences/JSON)
- tableau de bord avec indicateurs et graphiques
- saisie rapide des dépenses
- budget mensuel avec comparaison prévu/réel
- calendrier mensuel et filtre par mois
- caisse / solde de trésorerie
- suivi du prêt et objectif de remboursement anticipé
- export CSV partageable avec Excel
- sauvegarde JSON et restauration JSON via le sélecteur de fichiers Android

## Ouvrir
1. Décompresser le ZIP.
2. Ouvrir le dossier `MiniBudgetFamilial_V2` dans Android Studio.
3. Laisser Android Studio synchroniser Gradle.
4. Lancer sur un téléphone Android ou un émulateur.
5. Pour produire l'APK : Build > Build APK(s).

## Important
Le projet est le code source Android prêt à ouvrir, pas un APK compilé. L'environnement de génération utilisé ici ne dispose pas du SDK/Gradle Android nécessaires pour compiler et signer l'APK.

Les montants du budget et du prêt reprennent les données de travail déjà définies : loyer 200 000 Ar, mensualité 704 024 Ar, capital suivi 23 516 927 Ar au 15/09/2026, et épargne de remboursement anticipé 5 M Ar déjà disponibles + 5 M Ar en juin + 5 M Ar en décembre. Le montant réel de remboursement anticipé doit être confirmé par la banque.
