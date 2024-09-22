# Crazy Shopping Assistant
Basic webshop 'Bizarre Bazaar' with AI-powered shopping assistant with two modes:
- **Help the customer** select the best products for their needs
- **Help the shop** by all (unethical) means to sell the more and more expensive products

This is an instructional app that aims at demonstrating how to build an AI-powered apps
- with and without programmatical state management
- with and without human-in-the-loop
- with and without ethical considerations
- how your AI system could be hacked (by LLMs or humans)


## Launch instructions
In terminal, run `quarkus dev` in the project root directory

On the main branch:
1. The webshop with helpful stateful assistant will be served at `http://localhost:8080/helpful-assistant.html`
2. The webshop with helpful stateless assistant (LLM decides on transitions) will be served at `http://localhost:8080/no-state-assistant.html`

On the helpful-with-confirmation branch:
1. The webshop with helpful stateful assistant asking for human confirmation, will be served at `http://localhost:8080/helpful-assistant.html`

On the manipulative-assistant branch:
1. The webshop with unethical assistant will be served at `http://localhost:8080/unethical-assistant.html`
