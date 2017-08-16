# Game of Life

![gameoflifev1](https://user-images.githubusercontent.com/10222413/29358511-ee92eb52-8298-11e7-97c5-cd76f9132293.gif)

This program helps you **experiment** with **Conway's game of life**.

> -------------

## For those who haven't heard of the game,

The game is a zero-player game, meaning that its evolution is determined by its initial state, requiring no further input. One interacts with the Game of Life by creating an initial configuration and observing how it evolves.

## Rules

The universe of the Game of Life is a two-dimensional grid of square cells, each of which is in one of two possible states, alive or dead, or "populated" or "unpopulated". Every cell interacts with its eight neighbours, which are the cells that are horizontally, vertically, or diagonally adjacent. At each step in time, the following transitions occur:

1. Any live cell with fewer than two live neighbours dies, as if caused by underpopulation.
2. Any live cell with two or three live neighbours lives on to the next generation.
3. Any live cell with more than three live neighbours dies, as if by overpopulation.
4. Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.

The initial pattern constitutes the seed of the system. The first generation is created by applying the above rules simultaneously to every cell in the seed—births and deaths occur simultaneously, and the discrete moment at which this happens is sometimes called a tick (in other words, each generation is a pure function of the preceding one). The rules continue to be applied repeatedly to create further generations.

You can read more on it [here](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life).

> ---------------

## Running the program

1. Compile: `javac Test.java`.
2. Run: `java Test`.
