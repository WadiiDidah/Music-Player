from flask import Flask, jsonify, request

from app.command_parser import parse_command

app = Flask(__name__)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/commands")
def commands():
    payload = request.get_json(silent=True) or {}
    command = parse_command(str(payload.get("command", "")))
    return jsonify({"action": command.action, "track": command.track})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=False)
