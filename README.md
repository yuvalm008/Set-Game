# Set Game Project

This project is part of the SPL course at Ben-Gurion University. It focuses on implementing a Set Game, with an emphasis on utilizing threads and thread synchronization. We have added the assigment intructions.

## Description

The Set Game is a pattern recognition card game where players identify sets of three cards based on certain features. The project aims to implement the game logic and user interface, allowing players to interactively play the game.

## Features

- **Game Logic:** Implements the rules and mechanics of the Set Game.
- **User Interface:** Provides a graphical interface for players to interact with the game.
- **Thread Handling:** Utilizes threads for handling game logic and user input to ensure smooth gameplay.
- **Thread Synchronization:** Implements synchronization mechanisms to prevent race conditions and ensure data integrity in a multi-threaded environment.

## Implemented Classes (Under "ex" Package)

```java
1. Card

- Represents a single card in the Set Game.
- Contains attributes such as shape, color, number, and shading.

2. Deck

- Represents a deck of cards.
- Responsible for shuffling the cards and dealing them to players.

3. Player

- Represents a player in the Set Game.
- Manages the player's hand and score.

4. SetGame

- Main class responsible for orchestrating the Set Game.
- Handles game initialization, player turns, and determining the winner.
```

## Getting Started

To get started with the Set Game project, follow these steps:

1. **Clone the Repository:** Use the following command to clone the repository to your local machine:
   ```
   git clone https://github.com/yuvalm008/Set-Game.git
   ```

2. **Navigate to the Project Directory:** Enter the project directory:
   ```
   cd Set-Game
   ```

3. **Compile and Package the Project with Maven:** Use Maven to compile the source code and package it into an executable JAR file:
   ```
   mvn clean package
   ```

4. **Run the Game:** Execute the generated JAR file to launch the Set Game:
   ```
   java -jar target/SetGame.jar
   ```
