var appModule = angular.module('myApp', []);

appModule.controller('MainCtrl', ['authService','$scope','$http',
    function(authService, $scope, $http) {
        $scope.greeting = 'Welcome to the JSON Web Token / AngularJR / Spring example!';
        $scope.token = null;
        $scope.error = null;
        $scope.roleUser = false;
        $scope.roleAdmin = false;
        $scope.roleFoo = false;
        $scope.name = "";

        $scope.authenticate = function() {
            $scope.error = null;
            authService.validate(localStorage.getItem('JWT')).then(function(token) {
                // Token may be null if we are redirecting
                if(token !== null) {
                    $scope.token = token;
                    $http.defaults.headers.common.Authorization = 'Bearer ' + token;
                    $scope.checkRoles();
                    authService.whoami().then(function(loginId) {
                        $scope.name = loginId;
                    });
                    localStorage.setItem('JWT', token);
                }
            },
            function(error){
                $scope.error = error
                $scope.userName = '';
            });
        }

        $scope.checkRoles = function() {
            authService.hasRole('user').then(function(user) {$scope.roleUser = user});
            authService.hasRole('admin').then(function(admin) {$scope.roleAdmin = admin});
            authService.hasRole('foo').then(function(foo) {$scope.roleFoo = foo});
        }

        $scope.logout = function() {
            $scope.userName = '';
            $scope.token = null;
            $http.defaults.headers.common.Authorization = '';
        }

        $scope.loggedIn = function() {
            return $scope.token !== null;
        }

        $scope.authenticate();
    }
]);



appModule.service('authService', function($http, $window) {
    return {
        validate : function(token) {
            return $http.post('/auth/validate', {token: token}).then(function(response) {
                if(response.data.token) {
                    return response.data.token;
                } else {
                    // Received a request to redirect to CAS. Obey.
                    localStorage.removeItem("JWT");
                    $window.location.href = response.data.redirect;
                    return null;
                }
            });
        },

        hasRole : function(role) {
            return $http.get('/api/role/' + role).then(function(response){
                console.log(response);
                return response.data;
            });
        },

        whoami : function() {
            return $http.get('/api/whoami/').then(function(response){
                return response.data.loginId;
            });
        }
    };
});
