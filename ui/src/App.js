// @flow

import React, { Component } from 'react';
import Grid from './Grid';
import './App.css';

type Coord = [number, number];

export class Board {
  width: number;
  height: number;
  squares: Map<Coord, string>;
  constructor(width: number, height: number, squares: Map<Coord, string>) {
    this.width = width;
    this.height = height;
    this.squares = squares;
  }
}

class App extends Component {
  render() {
    return (
      <div className="App">
        <section className="grid-view">
          <Grid
            board={{
              width: 2,
              height: 2,
              squares: new Map([[[0, 0], 'a'], [[1, 1], 'b']]),
            }}
          />
        </section>
        <section className="grid-view">
          <Grid
            board={{
              width: 2,
              height: 2,
              squares: new Map([[[0, 0], 'a'], [[1, 1], 'b']]),
            }}
          />
        </section>
      </div>
    );
  }
}

export default App;
