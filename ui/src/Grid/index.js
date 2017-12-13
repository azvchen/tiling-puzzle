// @flow
import React from 'react';
import toMaterialStyle from 'material-color-hash';
import { Board, Tile } from '../App';
import './styles.css';

type Props = {
  board: Board,
  mini?: boolean,
  tiles?: ?(Tile[]),
};

class Grid extends React.Component<Props> {
  shouldComponentUpdate(nextProps) {
    let tilesEqual =
      this.tiles === undefined
        ? nextProps.tiles === undefined
        : this.tiles === null && nextProps.tiles === null;
    if (!tilesEqual && this.tiles && nextProps.tiles) {
      // both tiles are defined, check tile equality
      tilesEqual =
        this.tiles.length === nextProps.tiles.length &&
        this.tiles.every((tile, i) => tile.equals(nextProps.tiles[i]));
    }
    return !tilesEqual || !this.props.board.equals(nextProps.board);
  }

  render() {
    const { board, tiles, mini } = this.props;
    if (board.isEmpty()) {
      return <h1>No board.</h1>;
    }
    if (tiles === null) {
      // undefined means this is the reference board
      return <h1>No solution.</h1>;
    }
    const table = [...Array(board.height)].map(() =>
      [...Array(board.width)].map(() => ({
        color: null,
        borders: [],
      })),
    );

    for (const [[r, c], color] of board.squares) {
      table[r][c].color = color;
    }

    if (tiles) {
      // find borders of tiles
      const adjacentOffsets = {
        borderTop: [-1, 0],
        borderBottom: [1, 0],
        borderLeft: [0, -1],
        borderRight: [0, 1],
      };
      for (const tile of tiles) {
        const [dr, dc] = tile.position;
        for (const [[r, c]] of tile.squares) {
          const { borders } = table[r + dr][c + dc];
          for (const [location, [ar, ac]] of Object.entries(adjacentOffsets)) {
            if (!tile.has([r + ar, c + ac])) {
              borders.push(location);
            }
          }
        }
      }
    }

    return (
      <table className="grid">
        <tbody className={mini ? 'mini' : null}>
          {table.map((row, i) => (
            <tr key={i}>
              {row.map(({ color, borders }, j) => (
                <td
                  className="cell"
                  key={`${color}-${j}`}
                  style={Object.assign(
                    color ? toMaterialStyle(color) : { background: 'none' },
                    { border: '0 solid white' },
                    Grid.toBorderStyles(borders),
                  )}
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

  static toBorderStyles(borders: string[]) {
    if (!borders.length) {
      return {};
    }
    const borderStyles = {
      borderTopWidth: '0',
      borderBottomWidth: '0',
      borderLeftWidth: '0',
      borderRightWidth: '0',
    };
    for (const border of borders) {
      borderStyles[`${border}Width`] = '1px';
    }
    return borderStyles;
  }
}

export default Grid;
