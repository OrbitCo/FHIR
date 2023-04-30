import { Config } from "../models/config.model";

const config: Config = {
    EMAIL_PATTERN: '[a-zA-Z0-9.-_]{1,}@[a-zA-Z.-]{2,}[.]{1}[a-zA-Z]{2,}',
    NAME_PATTERN: '^[a-zA-Z ]*$',
}

export default config;