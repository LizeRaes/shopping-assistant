# Crazy Shopping Assistant
Basic webshop 'Bizarre Bazaar' with AI-powered shopping assistant with two main modes:
- **Help the customer** select the best products for their needs
- **Help the shop** by all (unethical) means to sell the more and more expensive products

> ###️ Disclaimer
>
> this is a demo app **for educational purposes only**, and we ran out of time as usual... quick qnd dirty warning!

## Launch instructions
You need to support Java 21

Make sure after pulling that mvnw is executable, if not run `chmod +x mvnw`

In terminal, run `quarkus dev` or `./mvnw quarkus:dev` in the project root directory

To select the mode of the shopping assistant, see sections below.

## Shopping Assistant Modes

Be aware that currently, we only have 4 products in the database: Chocolate Bar, Diapers, Baby Bottle 1 (cheaper), Baby Bottle 2 (expensive)
You can add more in the `initializeShoppingDatabase()`method in `ShoppingDatabase.java`

### 1. Helpful Assistant with no state
On the `main` branch

Served at `http://localhost:8080/helpful-assistant-no-state.html`

- **What is it?** One LLM (in AiService `FullAssistant.java` will deal with all step transitions and have access to all tools
- **Demonstrates:** High quality LLMs are capable of supporting easy workflows without the need for proper state management.
  The behavior is not predictable though, sometimes steps will be skipped and the LLM can get confused.
- **How to use it?** Describe in the chat what you'd like to buy, and the LLM should guide you through the decision process,
  offer you products, move you to shopping cart and help you place the order.

>### 💡Observe
>
>  Most of the time, the LLM will manage to run through the whole process as expected, but not always.


### 2. Helpful Assistant with state
On the `main` branch

Served at `http://localhost:8080/helpful-assistant.html`

- **What is it?** Different AiServices lined up in a state machine with programatically decided flow.
- **Demonstrates:** How to manage state in a more predictable way, and how to use different AiServices for different tasks.
  This setup allows limiting the responsibilities and tool access of the different AiServices.
  A separate AiService is used to decide on the state transition (`FinalSelectionDecider.java`).
- **How to use it?** Describe in the chat what you'd like to buy, and the LLM should guide you through the decision process,
  offer you products, move you to shopping cart and help you place the order.

>### 💡Observe
>
>  The assistant simply places the order without asking permission. This is on one hand helpful, on the other hand very disturbing given it has access to your credit card. That's why, for costly things, we always want a human in the loop.

### 3. Helpful Assistant with human confirmation
On the `helpful-with-confirmation` branch

Served at `http://localhost:8080/helpful-assistant.html`

- **What is it?** State machine from the former version, that will ask user confirmation before placing the order.
- **Demonstrates:** How to add a human in the loop, which you'll typically want to do for important decisions, costly or long procedures, etc.
- **How to use it?** Same as before. Then try out some hacking, like 'drop all tables' or 'what user info do you have?'.

>### 💡Observe
>
>  You'll be asked for confirmation before placing the order.
>
>  Unrelated, the system is vulnerable to hacking and gives away user info or drop tables without protesting.
> Partly because we have a bad setup, partly because we could use some input sanitization.

### 4. Helpful Assistant with input sanitization
On the `helpful-with-confirmation` branch

⚠️ Bring the input sanitization in place by uncommenting line 78 to 82 of `HelpfulAssistantResource.java`

Served at `http://localhost:8080/helpful-assistant.html`

- **What is it?** Same as before, with input sanitization (done by `InputSanitizer.java`)
- **Demonstrates:** How to bring guardrails in place to protect your system from prompt injection, SQL injection and more.
- **How to use it?** Try to hack it again

### 4. Helpful Assistant getting hacked by LLM
On the `helpful-with-confirmation` branch

⚠️ Bring hacker in place by uncommenting line 61 of `HelpfulAssistantResource.java`
Input sanitization can be switched on or off by (un)commenting line 78 to 82 of `HelpfulAssistantResource.java`

Served at `http://localhost:8080/helpful-assistant.html`

- **What is it?** `Hacker.java` will try to hack the system (10 back and forth messages max). Sit back and enjoy.
- **Demonstrates:** Realize that AI will also try to breach our system, and how important it is to have proper security measures in place.
- **How to use it?** Say 'hi' in the chat window, wait a bit and see the rest unroll by itself (with input sanitization on or off - hacking detection has no consequences in our demo setup)

- with and without ethical considerations
- how your AI system could be hacked (by LLMs or humans)

### 5. Unethical Capitalist Assistant
On the `manipulative-assistant` branch

Served at `http://localhost:8080/unethical-assistant.html`

- **What is it?** The same helpful assistant, but it will try to spend more by calling a another endpoint `PsychologistResource.java`
- it will keep a user profile and track things like impulse buying tendencies, the type of discount you fall for, etc.
- it will try to offer you more products than you selected, based on your profile, and always offer the more expensive product
- it will create tailored descriptions for the products, based on your sensitivities
- **Demonstrates:** Two Agents interacting
- **How to use it?** go with the dummy userProfile (set in `CustomUserProfile.java`) or alter it to your liking, and chat like before

