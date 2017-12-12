// @flow
import React, { Component } from 'react';
import { IconButton, Snackbar } from 'material-ui';
import Grid from './Grid';
import Sidebar from './Sidebar';
import settings from './settings';
import { MockBoard, MockSolution } from './MockData';
import './App.css';

type Coord = [number, number];

function mapEquals(map1: Map<Coord, string>, map2: Map<Coord, string>) {
  if (map1.size !== map2.size) {
    return false;
  }
  for (const [key, val] of map1) {
    const testVal = map2.get(key);
    // in cases of an undefined value, make sure the key
    // actually exists on the object so there are no false positives
    if (testVal !== val || (testVal === undefined && !map2.has(key))) {
      return false;
    }
  }
  return true;
}

export class Board {
  width: number;
  height: number;
  squares: Map<Coord, string>;
  coords: Map<string, boolean>;

  constructor(width: number, height: number, squares: Map<Coord, string>) {
    this.width = width;
    this.height = height;
    this.squares = squares;
    this.coords = new Map();
    for (const [coord] of squares) {
      this.coords.set(coord.toString(), true);
    }
  }

  isEmpty() {
    return !this.width || !this.height;
  }

  equals(otherBoard: Board): boolean {
    return (
      this.width === otherBoard.width &&
      this.height === otherBoard.height &&
      mapEquals(this.squares, otherBoard.squares)
    );
  }

  has(coord: Coord): boolean {
    return this.coords.has(coord.toString());
  }
}

export class Tile extends Board {
  position: Coord;

  constructor(board: Board, position: Coord) {
    super(board.width, board.height, board.squares);
    this.position = position;
  }
}

type Solution = Tile[];

type Props = {};

type State = {
  board: Board,
  selectedSolution: number,
  snackbarMessage: string,
  snackbarOpen: boolean,
  socket: WebSocket,
  solutions: Solution[],
};

function createSocket(app: App, timeout: number): WebSocket {
  timeout = Math.min(timeout || 500, 8 * 1000);
  const socket = new WebSocket('ws://localhost:8080/ws');
  socket.addEventListener('open', () => {
    console.log('connected');
    app.setState({ snackbarOpen: true, snackbarMessage: 'Connected' });
  });
  socket.addEventListener('close', () => {
    const closeMessage = 'Connection lost, retrying...';
    console.warn('connection lost');
    if (app.state.snackbarMessage !== closeMessage) {
      app.setState({ snackbarOpen: true, snackbarMessage: closeMessage });
    }
    setTimeout(() => {
      app.setState({ socket: createSocket(app, timeout * 2) });
    }, timeout);
  });
  socket.addEventListener('message', e => app.handleMessage(e.data));
  return socket;
}

class App extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    const solution: Solution = [];
    for (const [position, board] of Object.entries(MockSolution)) {
      solution.push(
        new Tile(
          this.deserializeBoard(board),
          position.split(' ').map(i => +i),
        ),
      );
    }
    this.state = {
      board: this.deserializeBoard(MockBoard),
      selectedSolution: 0,
      snackbarOpen: false,
      snackbarMessage: '',
      socket: createSocket(this),
      solutions: [],
    };
  }

  render() {
    let {
      board,
      selectedSolution,
      snackbarMessage,
      snackbarOpen,
      solutions,
    } = this.state;
    const closeSnackbar = () => this.setState({ snackbarOpen: false });

    return (
      <div className="App">
        <section className="grid-view">
          <Grid board={board} />
        </section>
        <section className="grid-view">
          <Grid
            board={board}
            tiles={solutions.length ? solutions[selectedSolution] : null}
          />
        </section>
        <section className="sidebar-view">
          <Sidebar
            settings={settings}
            onSave={settings => this._onSave(settings)}
            onSubmit={() => this._onSubmit()}
          />
        </section>
        <Snackbar
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          autoHideDuration={6000}
          open={snackbarOpen}
          onRequestClose={closeSnackbar}
          SnackbarContentProps={{
            'aria-describedby': 'message-id',
          }}
          message={<span id="message-id">{snackbarMessage}</span>}
          action={[
            <IconButton
              key="close"
              aria-label="Close"
              color="inherit"
              onClick={closeSnackbar}
            >
              close
            </IconButton>,
          ]}
        />
      </div>
    );
  }

  handleMessage(message: string) {
    const delimiterIndex = message.indexOf(' ');
    let command = message.slice(0, delimiterIndex);
    let data = message.slice(delimiterIndex + 1);
    if (delimiterIndex === -1) {
      command = data;
      data = null;
    }
    console.log(command, data);
    switch (command) {
      case 'board':
        this.updateBoard(data);
        break;
      case 'solution':
        this.addSolution(data);
        break;
    }
  }

  updateBoard(boardString: string) {
    const board = this.deserializeBoard(JSON.parse(boardString));
    this.setState({ board });
  }

  addSolution(serializedSolutions: string) {
    const solution: Solution = [];
    for (const [position, board] of Object.entries(
      JSON.parse(serializedSolutions),
    )) {
      solution.push(
        new Tile(
          this.deserializeBoard(board),
          position.split(' ').map(i => +i),
        ),
      );
    }
    this.setState({ solutions: this.state.solutions.concat([solution]) });
  }

  deserializeBoard(
    serialized: ?{ width: number, height: number, squares: any },
  ): Board {
    if (!serialized) {
      return new Board(0, 0, new Map());
    }
    const { width, height, squares } = serialized;
    const squareMap = new Map();
    for (const [position, color] of Object.entries(squares)) {
      squareMap.set(position.split(' ').map(i => +i), color);
    }
    return new Board(width, height, squareMap);
  }

  _onSave(settings: settings) {
    const serverSettings = { ...settings };
    const reader = new FileReader();
    reader.onload = event => {
      serverSettings.puzzle = event.target.result;
      this.state.socket.send(`settings ${JSON.stringify(serverSettings)}`);
    };
    reader.readAsText(settings.puzzle);
  }

  _onSubmit() {
    this.state.socket.send('solve');
  }
}

export default App;
