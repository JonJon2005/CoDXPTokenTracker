# Python CLI

Interactive terminal tool for viewing and editing XP token counts stored in MongoDB.

## Run

```bash
cd python
pip install -r requirements.txt  # install bcrypt and optional tools
python main.py
```
Optionally install `colorama` for better colour support on Windows:
```bash
pip install colorama
```
The menu lets you inspect totals, adjust individual values or export summaries. Changes persist back to MongoDB.
