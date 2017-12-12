// @flow
import React, { Component } from 'react';
import { IconButton, Snackbar } from 'material-ui';
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
  snackbarMessage: string,
  snackbarOpen: boolean,
  socket: WebSocket,
};

function createSocket(app: App): WebSocket {
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
      app.setState({ socket: createSocket(app) });
    }, 1000);
  });
  socket.addEventListener('message', e => app.handleMessage(e.data));
  return socket;
}

class App extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      socket: createSocket(this),
      snackbarOpen: false,
      board: new Board(0, 0, new Map()),
    };
  }

  render() {
    let { board, snackbarMessage, snackbarOpen } = this.state;
    const closeSnackbar = () => this.setState({ snackbarOpen: false });

    return (
      <div className="App">
        <section className="grid-view">
          <Grid board={board} />
        </section>
        <section className="grid-view">
          <Grid board={board} />
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
    const board = this.stringToBoard(JSON.parse(boardString));
    this.setState({ board });
  }

  addSolution(serializedSolutions: string) {
    const solution: Solution = [];
    for (const [position, board] of Object.entries(
      JSON.parse(serializedSolutions),
    )) {
      solution.push(new Tile(board, position));
    }
    this.setState({ solutions: this.state.solutions.concat([solution]) });
  }

  stringToBoard(
    serialized: ?{ width: number, height: number, squares: any },
  ): Board {
    if (!serialized) {
      return new Board(0, 0, new Map());
    }
    const { width, height, squares } = serialized;
    const squareMap = new Map();
    for (const [position, color] of Object.entries(squares)) {
      squareMap.set(position.split(' '), color);
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
