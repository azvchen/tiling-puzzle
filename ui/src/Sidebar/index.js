// @flow

import type { SettingType } from '../settings';
import settings from '../settings';
import React from 'react';
import {
  Button,
  FormControlLabel,
  FormGroup,
  IconButton,
  Input,
  Snackbar,
  Switch,
} from 'material-ui';
import './styles.css';

type Props = {
  settings: settings,
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

function Option({ name, value, onChange }): Switch | Input {
  const inputType = typeMap.get(typeof value);
  switch (inputType) {
    case 'checkbox':
      return (
        <FormControlLabel
          control={<Switch checked={value} onChange={onChange} value={name} />}
          label={name}
        />
      );
    case 'number':
    case 'file':
      return <Input type={inputType} onChange={onChange} />;
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
    let { snackbarOpen, ...settings } = this.state;
    const closeSnackbar = () => this.setState({ snackbarOpen: false });

    return (
      <form className="sidebar">
        <h1>Settings</h1>
        <FormGroup>
          {Object.entries(settings).map(([key, value]) => (
            <Option
              key={key}
              name={key}
              value={value}
              onChange={e => this._onChange(key, e.target)}
            />
          ))}
        </FormGroup>
        <Button onClick={() => this._submit()}>Save</Button>
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

  _submit() {
    this.setState({
      snackbarOpen: true,
    });
  }
}

export default Sidebar;
