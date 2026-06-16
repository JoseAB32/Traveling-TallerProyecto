import {TranslocoGlobalConfig} from '@jsverse/transloco-utils';
    
const config: TranslocoGlobalConfig = {
  rootTranslationsPath: 'public/i18n/',
  langs: [ 'es', 'en', 'fr', 'pt', 'it' ],
  keysManager: {}
};
    
export default config;