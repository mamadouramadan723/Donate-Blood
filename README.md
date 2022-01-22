# giveblood
Après avoir téléchargé et ouvert le projet sur Android studio, il faudra :

1 - Suppimer le fichier google-services.json ( dans le fichier "GiveBlood\app\google-services.json")

2 - Connecter son Application à un projet Firebase , il existe deux méthodes pour celà ( voir https://firebase.google.com/docs/android/setup)

3- Utiliser les dernières versions de "com.firebaseui:firebase-ui-auth" et "com.firebaseui:firebase-ui-firestore" (voir https://mvnrepository.com/artifact/com.firebaseui/) (dans le fichier "GiveBlood\app\build.gradle")

4 - Si necessaire rajouter jCenter() après mavenCentral() (dans le fichier "GiveBlood\build.gradle")

Et tout devrait normalement fonctionner.
