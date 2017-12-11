// @flow

import React from 'react';
import settings from '../settings';
import './styles.css';
import type { SettingType } from '../settings';

type Props = {
  settings: settings,
};

type State = settings;

type Inputs = 'checkbox' | 'number' | 'file';

const typeMap: Map<string, Inputs> = new Map([
  ['boolean', 'checkbox'],
  ['number', 'number'],
  ['object', 'file'],
]);

function Option({ name, value, onChange }) {
  const inputType = typeMap.get(typeof value);
  // error, type not supported
  if (!inputType) return this;
  const inputId = `id-label-${name}`;
  return (
    <React.Fragment>
      <label htmlFor={inputId}>
        {name}
        {inputType === 'file' && <br />}
        <input
          id={inputId}
          type={inputType}
          checked={value}
          onChange={onChange}
        />
      </label>
    </React.Fragment>
  );
}

class Sidebar extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
    for (const [key, value] of props.settings.entries()) {
      this.state[key] = value;
    }
    console.log(this.state);
  }
  render() {
    let settings = this.state;
    return (
      <form className="sidebar">
        <h1>Settings</h1>
        {Object.entries(settings).map(([key, value]) => (
          <Option
            key={key}
            name={key}
            value={value}
            onChange={e => this._onChange(key, e.target)}
          />
        ))}
        <button>Save</button>
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
}

export default Sidebar;
