# COMP2000 Assignment 2 - Xaishan Persad 47715383

## Overview
This project is a grid-based Java game. The world is a 20x20 grid of cells with three terrain types: grass, sand, and water. Actors (Dog, Cat, Bird) move turn-by-turn; collectible objects (Tree on grass, Fish in water, Cactus on sand) spawn periodically and can be collected for points (Tree = 1, Fish/Cactus = 2). The Cat has health (hearts) and terrain-specific health (bubbles on water, sun on sand) that deplete over time and on movement. You win at 5 points, you lose if the Cat’s health reaches 0 or if any bot reaches 3 points.

## Design patterns
- Inheritance and polymorphism
	- Classes: `Actor` (abstract), concrete actors `Dog`, `Cat`, `Bird`, terrains implement `TerrainType` (`GrassTerrain`, `SandTerrain`, `WaterTerrain`).
	- Contribution: Shared rendering/move/turn state in `Actor` reduces duplication, terrain behavior is encapsulated per type, new actors/terrains can be added without changing core logic.

- State pattern (input/turn flow)
	- Interfaces and states: `GameState` with `ChoosingActor`, `SelectingNewLocation`, `BotMoving`.
	- Contribution: Clean separation of concerns for user input vs. bot phase vs. selection overlay, easy to extend behavior for each phase without conditionals scattered across the code. 

- Observer pattern (decoupled weather updates)
	- Components: `WeatherEventBus` (subject), `WeatherListener` (observer interface), `WeatherObserver` (concrete observer), `Client` (publisher via HTTP stream), wiring in `GameManager`.
	- Contribution: Fully decouples network ingestion from the grid model and rendering. The UI can run smoothly while weather events arrive asynchronously and are applied to the grid through a single, testable observer.

- Strategy pattern (bot targeting and movement style)
	- Bot targeting: `TargetingStrategy` with `NearestObjectStrategy` used by `BotMoving` to choose a next cell among available moves.
	- Movement style: `MoveStrategy` is swapped based on location parity inside `Actor.setLocation` for simple, pluggable movement per step.
	- Contribution: Allows swapping or extending bot decision-making without modifying the bot loop keeps movement heuristics encapsulated and testable.

- Iterator pattern (grid traversal)
	- `Grid implements Iterable<Cell>` and also exposes `squareIterable(minX, minY, maxX, maxY)` for iterating a subregion.
	- Contribution: Makes neighborhood and region logic simpler and safer than manual index juggling; improves readability and supports for-each idioms.

- Renderer abstraction (decorator-ready)
	- Interfaces: `ActorRenderer` with `BaseActorRenderer`, `Stage` calls `actorRenderer.render(g, player)`.
	- Contribution: Establishes a seam to layer additional visual behaviors as decorators without touching actor drawing or `Stage`. The current build uses the base component, adding a decorator is a drop-in extension.

## Streams and lambdas
- `Stage`
	- Compute “humans with moves left” during the player phase: filter non-bot actors and exclude decorations, then `count()`.
	- Find the single `Cat` instance via `stream().filter(...).map(...).findFirst()`.
	- Purge decorations that no longer match their terrain using `stream().filter(...).collect(toList())`.
	- On movement, detect and collect any decoration at the destination with a `findFirst()` filter chain.

- `ChoosingActor`
	- On click, select the human actor under the cursor with a `stream().filter(...).findFirst()` instead of nested loops.

- `SelectingNewLocation`
	- Resolve the clicked destination from the overlay list via `stream().filter(...).findFirst()`.
	- Recompute the remaining human moves using a filter + `count()` pipeline.

- `Client`
	- Read the server-sent event (SSE) response lines using `reader.lines().forEach(line -> ...)`, then parse and forward to the weather bus.

Why this helps
- Reduces boilerplate for find/filter/count operations on small collections (actors, overlay cells).
- Improves readability by keeping intent in a single expression.

## How values from the server are interpreted

The `Client` connects to the provided server and reads a newline-delimited stream. Each line is expected to have five tokens:

<timestamp> <weatherType> <x> <y> <value>

Example: `2025-11-02T12:00:01Z rainfall 2 -1 0.42`

Coordinate system and mapping
- The servers origin (0,0) is at grid cell J9. Positive x moves east, positive y moves south.
- `Grid.serverToGridCol/Row` and `Grid.cellAtServerCoords` map server coordinates to grid indices safely.

Weather types and units
- Temperature: `temp` values are normalised and mapped in `Cell.updateWeather` as 0-50°C.
- Rain: `rain` values are normalised and stored as-is. Rainfall decays gradually each tick so transient spikes fade naturally.
- Wind:
	- Legacy single value: `wind` is treated in `WeatherObserver` as a strength indicator (roughly 0-100). It is used to redistribute a fraction (up to 50%) of rainfall from the center cell to its 8 neighbors, simulating gust-driven displacement. The same value is also forwarded to cells so they can record wind magnitude.
	- Vector components: `windX`, `windY` are stored separately. `Cell.getWind()` reports the magnitude using components when available.

Application area and timing
- For most weather types, `WeatherObserver` applies effects to a square radius of 2 around the mapped center (via `Grid.squareIterable`), deferring terrain transitions to the per-frame `Grid.tick()`.
- For `wind`, `WeatherObserver` first redistributes rainfall and then notifies a slightly larger area about the wind itself.

Terrain transitions
- Flooding: high temperature and sustained rain can turn any terrain into water.
- Dry spells: when water has no rain for a while, it turns back to grass. hot, rainless cells trend towards sand.
- Additional rules cover grass to water under cool/wet conditions and sand to grass under sustained rain.
- All transitions are gradual weather values decay toward 0 in `Cell.tickDecay()`, terrains smooth their internal state in `TerrainType.tick`, and only then does `Grid.tick()` evaluate thresholds. This avoids jarring instant flips.
