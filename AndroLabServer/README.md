# AndroLab Backend Server

This project is the Python 3 backend server for the
[Android InsecureBankv2 application](https://github.com/dineshshetty/Android-InsecureBankv2).

## Required Python packages

Create a virtual environment in the project directory:

```bash
python3 -m venv .venv
```

Activate it before installing packages or running the server.

On macOS or Linux:

```bash
source .venv/bin/activate
```

On Windows:

```powershell
.venv\Scripts\Activate.ps1
```

To switch to the environment in a new terminal, run the activation command again.
When finished, you can leave the environment with:

```bash
deactivate
```

Install the required packages with pip:

```bash
python3 -m pip install -r requirements.txt
```

## Run the server

Make sure Python 3 is installed, then change to the `AndroLabServer` directory:

```bash
cd AndroLabServer
```

Start the HTTP server:

```bash
python3 app.py
```

To see the available command-line options:

```bash
python3 app.py --help
```
