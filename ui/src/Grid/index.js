// @flow
import React from 'react';
import toMaterialStyle from 'material-color-hash';
import { Board, Tile } from '../App';
import './styles.css';

type Props = {
  board: Board,
  tiles?: ?(Tile[]),
};

class Grid extends React.Component<Props> {
  shouldComponentUpdate(nextProps) {
    return !this.props.board.equals(nextProps.board) || nextProps !== undefined;
  }

  render() {
    const { board, tiles } = this.props;
    if (!board || board.isEmpty()) {
      return <h1>No board.</h1>;
    }
    if (tiles === null) {
      // undefined means this is the reference board
      return <h1>No solution.</h1>;
    }
    const table = [...Array(board.height)].map(() =>
      [...Array(board.width)].map(() => ({
        color: '',
        borders: {
          top: false,
          bottom: false,
          left: false,
          right: false,
        },
      })),
    );

    for (const [[r, c], color] of board.squares) {
      table[r][c].color = color;
    }

    if (tiles) {
      const adjacentOffsets = {
        top: [-1, 0],
        bottom: [1, 0],
        left: [0, -1],
        right: [0, 1],
      };
      for (const tile of tiles) {
        const [dr, dc] = tile.position;
        for (const [[r, c]] of tile.squares) {
          const { borders } = table[r + dr][c + dc];
          for (const [location, [ar, ac]] of Object.entries(adjacentOffsets)) {
            if (!tile.has([r + ar, c + ac])) {
              borders[location] = true;
            }
          }
        }
      }
    }

    return (
      <table class="grid">
        <tbody>
          {table.map((row, i) => (
            <tr key={i}>
              {row.map(({ color, borders }, j) => (
                <td
                  key={`${color}-${j}`}
                  style={Object.assign(
                    toMaterialStyle(color),
                    { border: '1px solid transparent' },
                    this._toBorderStyles(borders),
                  )}
                  className="cell"
                >
                  {color}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    );
  }

  _toBorderStyles(
    borderMask: ?{
      top: boolean,
      bottom: boolean,
      left: boolean,
      right: boolean,
    },
  ) {
    if (!borderMask) {
      return {};
    }
    const { top, bottom, left, right } = borderMask;
    const color = 'white';
    return {
      borderTopColor: top ? color : 'transparent',
      borderBottomColor: bottom ? color : 'transparent',
      borderLeftColor: left ? color : 'transparent',
      borderRightColor: right ? color : 'transparent',
    };
  }
}

export default Grid;
