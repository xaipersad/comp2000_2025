# COMP2000 Assignment 1 - Xaishan Persad 47715383

## Overview
This project is a simple grid-based game implemented in Java. The game features a 20x20 grid with grass, water, lava environments, three actors (Dog, Cat, Bird), and collectable items (circle, square, triangle). The player controls the Dog, aiming to collect 5 circles before the other actors collect their items.

## Object-Oriented Design: Inheritance
Inheritance is used to create a clear and extensible hierarchy for the actors in the game, as well as to support modular helper classes for game logic:

- **Actor (abstract class):** Defines shared properties and methods for all actors, such as their location and painting logic.
- **Dog, Cat, Bird (subclasses):** Each actor type extends `Actor` and implements its own display and movement logic. This allows for code reuse and easy addition of new actor types in the future.
- **Helper classes (e.g. ItemPlacer, GameResetHelper, EnvironmentHelper):** These classes encapsulate specific logic for item placement, game resetting, and environment checks. This modular approach, supported by inheritance and static methods, keeps the main game logic decluttered and maintainable.

This use of inheritance contributes to good design by:
- Reducing code duplication (shared logic in `Actor`)
- Making the codebase easier to maintain, extend, and modularise
- Allowing polymorphism (e.g. storing all actors in a `List<Actor>` and iterating over them generically)
- Supporting the creation of helper classes for single-responsibility logic (e.g., item placement, environment checks)

## Generics
Generics are used in the program to provide type safety and flexibility, especially with collections and helper classes:

- **List<Actor>:** The main list of actors is declared as `List<Actor>`, allowing the program to store any subclass of `Actor` (Dog, Cat, Bird) in a single collection. This enables polymorphic behaviour and simplifies code for iterating, painting, and updating actors.
- **Other collections:** Generics are also used for lists of cells (e.g. movement options), ensuring that only the correct types are stored and reducing runtime errors.
- **Helper classes:** Some helpers could be further extended with generics for even more reusable logic (e.g. generic managers for different types of items or actors).

This use of generics contributes to good design by:
- Enforcing compile-time type safety
- Making the code more readable and less error-prone
- Supporting flexible and reusable code structures
- Enabling modular helper classes to work with different types if needed

## Summary
By leveraging inheritance and generics, the program achieves a modularised, extensible, and type-safe design. The use of helper classes (such as `ItemPlacer`, `GameResetHelper`, and `EnvironmentHelper`) further declutters the code and makes it easier to add new features, maintain the code, and avoid common programming errors.

## Live Weather Integration
Assignment 2 extends the grid game with a continuously streaming weather feed that reshapes how the stage plays out:

- **HTTP weather stream:** `WeatherService` connects to `http://13.238.167.130/weather` and parses the live feed into `WeatherEvent` objects. The service uses a background daemon thread so the UI remains responsive while data arrives.
- **Observer pattern:** `WeatherService` notifies the `WeatherController`, which acts as an observer that keeps the latest `WeatherSnapshot` for every affected cell. These snapshots drive several visual and mechanical changes on the board.
- **Functional pipelines:** The incoming text feed is consumed with `BufferedReader.lines()`, mapped to events with `WeatherEvent::parse`, filtered using `Optional::stream`, and dispatched with lambda-based listeners. Within `WeatherController`, Java Streams are also used to build rainfall/temperature overlays and to update actors.
- **Weather-driven gameplay:**
  - **Flooding mechanic:** Rainfall above 70% marks a cell as flooded. Flooded tiles are removed from movement radii so humans and bots cannot step into deep water until the rain clears.
  - **Temperature feedback:** Actor brightness is controlled through a lambda pipeline that maps the latest temperature to a brightness modifier. Hot zones make actors glow, while cooler zones dim them.
  - **Wind-aware AI (Strategy pattern):** Bots swap from their usual `MoveRandomly`/`MoveLeft` strategies to a wind-biased `WindDrivenStrategy` whenever gusts are strong enough. This new strategy still composes with the base movement logic, but projects candidate moves onto the live wind vector supplied by the weather feed. It demonstrates both the Strategy pattern and lambda-based comparisons.
- **Visual overlays & HUD:** Each weather snapshot produces an alpha-blended overlay that tints the affected cell. Hovering a cell now reveals its rain, wind, and temperature percentages so the player can plan around the environment.

These changes rely on design patterns (Observer + Strategy), lambdas, and streams to keep the implementation concise while reacting intelligently to the live data feed.
