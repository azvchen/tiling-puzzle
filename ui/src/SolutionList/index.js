import type { Solution } from '../App';
import { Board } from '../App';
import { Chip } from 'material-ui';
import React from 'react';
import Grid from '../Grid';
import './styles.css';

type Props = {
  board: Board,
  selectedSolution: number,
  solutions: Solution[],
  onSelect: number => void,
};

class SolutionList extends React.Component<Props> {
  render() {
    const { board, selectedSolution, solutions, onSelect } = this.props;
    if (!solutions.length) {
      return <ol className="solution-list" style={{ height: 0 }} />;
    }
    return (
      <ol className="solution-list">
        {solutions.map((solution, i) => (
          <li
            className={`solution ${selectedSolution === i ? 'selected' : ''}`}
            key={i}
            onClick={() => onSelect(i)}
          >
            <Grid board={board} mini={true} tiles={solution} />
          </li>
        ))}
        <Chip
          className="solution-ticker"
          label={`${solutions.length} solution${
            solutions.length !== 1 ? 's' : ''
          } found.`}
        />
      </ol>
    );
  }
}

export default SolutionList;
