// @flow
import React, { Component } from 'react';
import { IconButton, Snackbar } from 'material-ui';
import Grid from './Grid';
import Sidebar from './Sidebar';
import SolutionList from './SolutionList';
import settings from './settings';
import './App.css';

type Coord = [number, number];

function mapEquals(map1: Map<Coord, string>, map2: Map<Coord, string>) {
  if (map1.size !== map2.size) {
    return false;
  }
  const tempMap = new Map();
  for (const [[r, c], value] of map2) {
    tempMap.set(`${r} ${c}`, value);
  }
  for (const [[r, c], val] of map1) {
    const key = `${r} ${c}`;
    const testVal = tempMap.get(key);
    // in cases of an undefined value, make sure the key
    // actually exists on the object so there are no false positives
    if (testVal !== val || (testVal === undefined && !tempMap.has(key))) {
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

  equals(otherTile: Tile): boolean {
    return (
      this.position[0] === otherTile.position[0] &&
      this.position[1] === otherTile.position[1] &&
      super.equals(otherTile)
    );
  }
}

export type Solution = Tile[];

type Props = {};

type State = {
  board: Board,
  isRunning: boolean,
  selectedSolution: number,
  snackbarMessage: string,
  snackbarOpen: boolean,
  socket: WebSocket,
  solutions: Solution[],
};

class App extends Component<Props, State> {
  createSocket(timeout: number): WebSocket {
    timeout = Math.min(timeout || 500, 8 * 1000);
    const socket = new WebSocket('ws://localhost:8080/ws');
    socket.addEventListener('open', () => {
      console.log('connected');
      this.showSnackbar('Connected');
    });
    socket.addEventListener('close', () => {
      const closeMessage = 'Connection lost, retrying...';
      console.warn('connection lost');
      if (this.state.snackbarMessage !== closeMessage) {
        this.showSnackbar(closeMessage);
        this.setState({ isRunning: false });
      }
      setTimeout(
        () => this.setState({ socket: this.createSocket(timeout * 2) }),
        timeout,
      );
    });
    socket.addEventListener('message', e => this.handleMessage(e.data));
    return socket;
  }

  constructor(props: Props) {
    super(props);
    this.state = {
      board: App.deserializeBoard(null),
      isRunning: false,
      selectedSolution: -1,
      snackbarOpen: false,
      snackbarMessage: '',
      socket: this.createSocket(this),
      solutions: [],
    };
  }

  render() {
    let {
      board,
      isRunning,
      selectedSolution,
      snackbarMessage,
      snackbarOpen,
      solutions,
    } = this.state;
    const closeSnackbar = () => this.setState({ snackbarOpen: false });
    if (selectedSolution < 0) {
      // no solution selected, auto-advance to latest solution
      selectedSolution = solutions.length - 1;
    }

    return (
      <div className="App">
        <div className="vertical-container">
          <div className="horizontal-container">
            <section className="grid-view">
              <Grid board={board} />
            </section>
            {!board.isEmpty() && (
              <section className="grid-view">
                <Grid
                  board={board}
                  tiles={solutions.length ? solutions[selectedSolution] : null}
                />
              </section>
            )}
          </div>
          <section className="solution-list-view">
            <SolutionList
              board={board}
              selectedSolution={selectedSolution}
              solutions={solutions}
              onSelect={selectedSolution => this.setState({ selectedSolution })}
            />
          </section>
        </div>
        <section className="sidebar-view">
          <Sidebar
            isRunning={isRunning}
            settings={settings}
            onSave={settings => this._onSave(settings)}
            onSubmit={() => this._onSubmit()}
          />
        </section>
        <Snackbar
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          autoHideDuration={3000}
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
    switch (command) {
      case 'board':
        this.updateBoard(data);
        break;
      case 'solution':
        this.addSolution(data);
        break;
      case 'solving':
        this.setState({ isRunning: true });
        this.resetSolutions();
        break;
      case 'solved':
        this.setState({ isRunning: false });
        const solutions = +data;
        this.showSnackbar(
          `Solved. ${solutions === 0 ? 'No' : solutions} solution${
            solutions === 1 ? '' : 's'
          } found.`,
        );
        break;
      default:
        console.warn('Unhandled command:', command, data);
        break;
    }
  }

  updateBoard(boardString: string) {
    const board = App.deserializeBoard(JSON.parse(boardString));
    this.resetSolutions();
    this.setState({ board });
  }

  addSolution(serializedSolutions: string) {
    const solution: Solution = [];
    for (const [position, board] of Object.entries(
      JSON.parse(serializedSolutions),
    )) {
      solution.push(
        new Tile(App.deserializeBoard(board), position.split(' ').map(i => +i)),
      );
    }
    this.setState({ solutions: this.state.solutions.concat([solution]) });
  }

  resetSolutions() {
    this.setState({ solutions: [], selectedSolution: -1 });
  }

  static deserializeBoard(
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
    if (this.abortIfSocketBroken()) return;
    const serverSettings = { ...settings };
    const reader = new FileReader();
    reader.onload = event => {
      serverSettings.puzzle = event.target.result;
      this.state.socket.send(`settings ${JSON.stringify(serverSettings)}`);
    };
    reader.readAsText(settings.puzzle);
  }

  _onSubmit() {
    if (this.abortIfSocketBroken()) return;
    this.state.socket.send('solve');
  }

  abortIfSocketBroken(): boolean {
    if (this.state.socket.readyState === WebSocket.OPEN) return false;
    this.showSnackbar('Connect to server first!');
    return true;
  }

  showSnackbar(message: string) {
    this.setState({ snackbarOpen: true, snackbarMessage: message });
  }
}

export default App;
