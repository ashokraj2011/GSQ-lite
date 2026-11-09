export COPILOT_PLUGIN_ROOT="$(python3 -c "import json,os;t=open(os.path.expanduser('~/.copilot/config.json')).read();t='\n'.join(l for l in t.splitlines() if not l.lstrip().startswith('//'));print([e['cache_path'] for e in json.loads(t)['installedPlugins'] if e['name']=='singularity-flow'][-1])")"

ls "$COPILOT_PLUGIN_ROOT/hook-launcher.sh"
