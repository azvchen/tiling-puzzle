// @flow

import React, { Component } from 'react';
import Grid from './Grid';
import Sidebar from './Sidebar';
import settings from './settings';
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

type Props = {};

type State = {
  socket: WebSocket,
};

class App extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      socket: new WebSocket('ws://localhost:8080/ws'),
    };
    this.state.socket.addEventListener('open', () => console.log('connected'));
    this.state.socket.addEventListener('message', e => console.log(e.data));
  }
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
              squares: new Map([[[0, 0], 'X'], [[1, 1], 'O']]),
            }}
          />
        </section>
        <section className="sidebar-view">
          <Sidebar settings={settings} />
        </section>
      </div>
    );
  }
}

export default App;
