// @flow
import React from 'react';
import toMaterialStyle from 'material-color-hash';
import { Board } from '../App';
import './styles.css';

type Props = {
  board: Board,
};

class Grid extends React.Component<Props> {
  render() {
    const { board } = this.props;
    if (!board) {
      return <h1>No board.</h1>;
    }
    const table = [...Array(board.height)].map(() => [...Array(board.width)]);

    for (const [[r, c], color] of board.squares) {
      table[r][c] = color;
    }

    return (
      <table id="grid">
        <tbody>
          {table.map((row, i) => (
            <tr key={i}>
              {row.map((col, j) => (
                <td
                  key={`${col}-${j}`}
                  style={toMaterialStyle(col || '')}
                  className="cell"
                >
                  {col || ''}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    );
  }
}

export default Grid;
