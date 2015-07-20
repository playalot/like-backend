requirejs.config({
    paths: {
        'underscore': '../lib/underscorejs/underscore',
        'jquery': 'lib/jquery.min',
        'react': '../lib/react/react-with-addons',
        'react-router': '../lib/react-router/build/global/ReactRouter',
        'react-bootstrap': '../lib/react-bootstrap/react-bootstrap',
        'json': "../lib/json3/json3",
        'moment': "../lib/momentjs/moment",
        'pikaday': "../lib/Pikaday",
        'medium-editor': "lib/medium-editor.min"
    },
    shim: {
        'underscore': { exports: '_' },
        'jquery': { exports: '$' },
        'json': { exports: 'JSON' },
        'react-bootstrap': { deps: ['jquery'] }
    },
    exclude: [
        "jquery",
        "underscore",
        "react",
        "bootstrap"
    ]
});

define("window", function () {
    return window;
});

require(["react", "components/App"],
    function (React, App) {

        function run() {
            React.render(
                React.createElement(App, null),
                document.getElementById("placeholder-app")
            );
        }

        if (window.addEventListener) {
            window.addEventListener('DOMContentLoaded', run);
        } else {
            window.attachEvent('onload', run);
        }

    }
);