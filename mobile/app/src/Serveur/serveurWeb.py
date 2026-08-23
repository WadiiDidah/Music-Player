from flask import Flask, jsonify, request
import os

app = Flask(__name__)

@app.route('/function1', methods=['POST'])
def route_function1():
    return ("okeey")

def function1(data):
   
    # code pour traiter le fichier audio
    return {data+" a été bien traité"}

@app.route('/function2', methods=['GET'])
def route_function2():
    file = request.files['file']
    filename = file.filename
    file.save(os.path.join('uploads', filename))
    result = function2(filename)
    return jsonify(result)

@app.route('/upload-audio', methods=['POST'])
def upload_audio():
    audio_data = request.data

    # Vérifier si les données sont vides
    if len(audio_data) == 0:
        return 'Aucune donnée audio n\'a été reçue'

    # Écrire les données dans un nouveau fichier audio
    with open('waddiI.mp3', 'wb') as f:
        f.write(audio_data)

    # Fermer le fichier audio
    f.close()
    
    requetteTal("lance Draganov lili")
    return jsonify(demande)


def function2(filename):
    # code pour traiter le fichier audio
    return {"result": "Fichier audio " + filename + " traité avec succès"}


demande = {
    "type_opeation": "",
    "nom": "",
}


def type_requette(req):
    req=req.lower()
    global type
    if "joue" in req.lower() or "play" in req.lower() or "commence" in req.lower() or "lance" in req.lower():
        if "joue" in req.lower():
            chanson_demandee_str = req.split("joue", 1)[1].strip()
        if "play" in req.lower():
            chanson_demandee_str = req.split("play", 1)[1].strip()
        if "commence" in req.lower():
            chanson_demandee_str = req.split("commence", 1)[1].strip()
        if "lance" in req.lower():
            chanson_demandee_str = req.split("lance", 1)[1].strip()

        type = {"type_opeation": "start"}
        chanson_demandee = {"nom": chanson_demandee_str}
    elif "stop" in req.lower() or "arrête" in req.lower():
        if "stop" in req.lower():
            chanson_demandee_str = req.split("stop", 1)[1].strip()
        if "arrête" in req.lower():
            chanson_demandee_str = req.split("arrête", 1)[1].strip()

        type = {"type_opeation": "stop"}
        chanson_demandee = {"nom": chanson_demandee_str}
    elif "supprimer" in req.lower() or "delete" in req.lower():
        if "supprimer" in req.lower():
            chanson_demandee_str = req.split("supprimer", 1)[1].strip()
        if "delete" in req.lower():
            chanson_demandee_str = req.split("delete", 1)[1].strip()
        type = {"type_opeation": "supprimer"}

        chanson_demandee = {"nom": chanson_demandee_str}
    else:
        type = {"type_opeation": ""}
        chanson_demandee = {"nom": ""}
    demande.update(type)
    demande.update(chanson_demandee)


def requetteTal(req):
    type_requette(req)






if __name__ == '__main__':
      app.run(host='0.0.0.0')
