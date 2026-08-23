//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

#pragma once

module Demo
{
    class Musique {
        string nom;
        string auteur;
        string type;
    };
    sequence<string> musiques;
    sequence<byte> test;

    interface Clock
    {
        void tick(string time);
        void addMusique(string genre,string nom,string auteur);
        void supprimerMusique(string genre,string nom,string auteur);
        void updateMusique(string genre,string oldNom, string nvNom,string auteur);

    }
}
