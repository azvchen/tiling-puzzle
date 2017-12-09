// @flow

import React, { Component } from 'react';
import Grid from './Grid';
import logo from './logo.svg';
import './App.css';

class App extends Component {
  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">Tiling Puzzle</h1>
        </header>
        <Grid />
      </div>
    );
  }
}

export default App;
