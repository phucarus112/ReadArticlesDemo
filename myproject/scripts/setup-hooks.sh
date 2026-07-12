#!/bin/sh
# Run once after cloning, from anywhere: sh myproject/scripts/setup-hooks.sh
# The git repo root is UPLIVE-Test; hooks live in myproject/.githooks
git config core.hooksPath myproject/.githooks
chmod +x myproject/.githooks/pre-commit
echo "Git hooks installed."
