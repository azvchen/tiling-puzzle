import React from 'react';

type Props = {
  started: boolean,
};

type State = {
  intervalHandle: ?number,
  secondsElapsed: number,
  startTime: ?number,
  endTime: ?number,
};

class Stopwatch extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      intervalHandle: null,
      lastUpdate: null,
      secondsElapsed: 0,
      startTime: null,
      endTime: null,
    };
  }

  shouldComponentUpdate(nextProps: Props, nextState: State) {
    return (
      nextProps.started !== this.props.started ||
      nextState.secondsElapsed !== this.state.secondsElapsed
    );
  }

  componentWillReceiveProps(nextProps: Props) {
    if (nextProps.started === this.props.started) {
      return;
    }
    if (nextProps.started) {
      // start timer
      this.setState({
        intervalHandle: requestAnimationFrame(() => this.updateSeconds()),
        secondsElapsed: 0,
        startTime: performance.now(),
        endTime: null,
      });
    } else {
      // stop timer
      cancelAnimationFrame(this.state.intervalHandle);
      this.setState({ intervalHandle: -1, endTime: performance.now() });
    }
  }

  render() {
    const { started } = this.props;
    const { secondsElapsed, startTime, endTime } = this.state;
    if (started) {
      return (
        <p>
          {((secondsElapsed - startTime) / 1000).toFixed(2)} seconds have
          passed.
        </p>
      );
    }

    if (startTime && endTime) {
      return (
        <p>Solve took {((endTime - startTime) / 1000).toFixed(5)} seconds.</p>
      );
    }

    return null;
  }

  updateSeconds() {
    const { started } = this.props;
    if (!started) return;
    let { secondsElapsed } = this.state;
    const now = performance.now();
    if (now - secondsElapsed >= 20) {
      secondsElapsed = now;
    }
    this.setState({
      intervalHandle: requestAnimationFrame(() => this.updateSeconds()),
      secondsElapsed,
    });
  }
}

export default Stopwatch;
