# Set Game Project

Welcome to the Set Game project! This project is part of the SPL course at Ben-Gurion University, designed to implement a digital version of the classic card game "Set". The focus of this project lies in threading and synchronization mechanisms, emphasizing smooth gameplay in a multi-threaded environment. We have added the assigment instructions to the reposetory

## About the Set Game

Set is a fast-paced card game where players must identify "sets" of three cards based on specific features. Each card has four attributes: shape (oval, squiggle, or diamond), color (red, green, or purple), number (one, two, or three), and shading (solid, striped, or open). A set consists of three cards in which each attribute is either all the same or all different across the three cards.

## Project Structure

The project comprises several components, including:

- **Game Logic:** Implements the rules and mechanics of the Set Game.
- **User Interface:** Provides a graphical interface for players to interact with the game.
- **Thread Handling:** Utilizes threads for handling game logic and user input to ensure smooth gameplay.
- **Thread Synchronization:** Implements synchronization mechanisms to prevent race conditions and ensure data integrity in a multi-threaded environment.

## Implemented Classes

Within the project, the following classes have been implemented:

1. **Card:** Represents a single card in the Set Game, featuring attributes such as shape, color, number, and shading.
2. **Deck:** Manages the deck of cards, responsible for shuffling and dealing cards to players.
3. **Player:** Represents a player in the Set Game, managing their hand and score.

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
