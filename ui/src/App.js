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

type Props = {};

type State = {
  snackbarMessage: string,
  snackbarOpen: boolean,
  socket: WebSocket,
};

function createSocket(
  component: Component,
  setState: State => void,
): WebSocket {
  const socket = new WebSocket('ws://localhost:8080/ws');
  socket.addEventListener('open', () => {
    console.log('connected');
    setState({ snackbarOpen: true, snackbarMessage: 'Connected' });
  });
  socket.addEventListener('close', () => {
    const closeMessage = 'Connection lost, retrying...';
    console.warn('connection lost');
    if (component.state.snackbarMessage !== closeMessage) {
      setState({ snackbarOpen: true, snackbarMessage: closeMessage });
    }
    setTimeout(() => {
      setState({ socket: createSocket(component, setState) });
    }, 1000);
  });
  socket.addEventListener('message', e => console.log(e.data));
}

class App extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      socket: createSocket(this, this.setState.bind(this)),
      snackbarOpen: false,
    };
  }

  render() {
    let { snackbarMessage, snackbarOpen } = this.state;
    const closeSnackbar = () => this.setState({ snackbarOpen: false });

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
