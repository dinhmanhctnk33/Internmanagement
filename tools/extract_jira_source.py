import ast
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def literal_assignment(path: Path, name: str):
    tree = ast.parse(path.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign):
            if any(isinstance(target, ast.Name) and target.id == name for target in node.targets):
                return ast.literal_eval(node.value)
    raise RuntimeError(f"Không tìm thấy biến {name} trong {path}")


backlog = literal_assignment(ROOT / "tools" / "build_sprint2_doc.py", "backlog_groups")
specific = literal_assignment(ROOT / "tools" / "build_sprint2_detailed_tasks.py", "specific")
story_context = literal_assignment(ROOT / "tools" / "build_sprint2_detailed_tasks.py", "story_context")

payload = {
    "groups": backlog,
    "specific": {str(key): value for key, value in specific.items()},
    "storyContext": story_context,
}

out = ROOT / "tmp" / "jira_source.json"
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
print(out)
