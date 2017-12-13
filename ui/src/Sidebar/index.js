// @flow

import type { SettingType } from '../settings';
import settings from '../settings';
import Stopwatch from '../Stopwatch';
import React from 'react';
import {
  Button,
  FormControlLabel,
  FormGroup,
  Icon,
  IconButton,
  Input,
  Snackbar,
  Switch,
} from 'material-ui';
import './styles.css';

type Props = {
  isRunning: boolean,
  settings: settings,
  onSave: settings => void,
  onSubmit: () => void,
};

type State = settings & {
  snackbarOpen: boolean,
};

type Inputs = 'checkbox' | 'number' | 'file';

const typeMap: Map<string, Inputs> = new Map([
  ['boolean', 'checkbox'],
  ['number', 'number'],
  ['object', 'file'],
]);

function Option({ disabled, name, value, onChange }): Switch | Input {
  const inputType = typeMap.get(typeof value);
  switch (inputType) {
    case 'checkbox':
      return (
        <FormControlLabel
          disabled={disabled}
          control={<Switch checked={value} onChange={onChange} value={name} />}
          label={name}
        />
      );
    case 'number':
      return <Input disabled={disabled} type={inputType} onChange={onChange} />;
    case 'file':
      return (
        <input
          className="file-picker"
          disabled={disabled}
          type={inputType}
          onChange={onChange}
        />
      );
    default:
      // error, type not supported
      return this;
  }
}

class Sidebar extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
    for (const [key, value] of props.settings.entries()) {
      this.state[key] = value;
    }
  }

  render() {
    const { isRunning } = this.props;
    let { snackbarOpen, ...settings } = this.state;
    const closeSnackbar = () => this.setState({ snackbarOpen: false });

    return (
      <form className="sidebar">
        <h1>Settings</h1>
        <FormGroup>
          {Object.entries(settings).map(([key, value]) => (
            <Option
              key={key}
              disabled={isRunning}
              name={key}
              value={value}
              onChange={e => this._onChange(key, e.target)}
            />
          ))}
        </FormGroup>
        <Button
          className="sidebar-button"
          raised
          color="primary"
          disabled={isRunning}
          onClick={() => this._save()}
        >
          Save<Icon>save</Icon>
        </Button>
        <Button
          className="sidebar-button"
          raised
          color="accent"
          disabled={isRunning}
          onClick={() => this._submit()}
        >
          Solve<Icon>send</Icon>
        </Button>
        <Stopwatch started={isRunning} />
        <Snackbar
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          autoHideDuration={6000}
          open={snackbarOpen}
          onRequestClose={closeSnackbar}
          SnackbarContentProps={{
            'aria-describedby': 'message-id',
          }}
          message={<span id="message-id">Saved.</span>}
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
      </form>
    );
  }

  _onChange(key, target) {
    const type = typeof this.state[key];
    let newValue: SettingType;
    switch (type) {
      case 'boolean':
        newValue = target.checked;
        break;
      case 'number':
        newValue = +target.value;
        break;
      case 'object':
        newValue = target.files[0];
        break;
      default:
        throw Error(`Unsupported setting type: ${typeof this.state[key]}`);
    }
    this.setState({
      [key]: newValue,
    });
  }

  _save() {
    this.setState({
      snackbarOpen: true,
    });
    this.props.onSave(this.state);
  }

  _submit() {
    this.props.onSubmit();
  }
}

export default Sidebar;
