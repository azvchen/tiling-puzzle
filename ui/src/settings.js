// @flow

export type SettingType = boolean | File | number;

const settings: Map<string, SettingType> = new Map([
  ['rotations', true],
  ['reflections', false],
  ['puzzle', new File([], 'none')],
]);

export default settings;
