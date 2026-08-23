module Example
{
    class Musique {
        string nom;
        string auteur;
        string type;
    };
    sequence<string> musiques;
    sequence<byte> test;
    interface MyInterface
    {
        void printMessage(string message);
        musiques getMusiques(string genre);
        bool supprimerMusique(string nom,string auteur);
        bool updateMusique(string oldNom, string nvNom,string auteur);
        test addMusique(string genre,string nom,string auteur);
        musiques getFavoris();
    };
};
