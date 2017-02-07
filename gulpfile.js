var gulp = require('gulp'),
    jshint = require('gulp-jshint'),
    sourcemaps = require('gulp-sourcemaps'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    gutil = require('gulp-util'),
    less = require('gulp-less'),
    minifyCSS  = require('gulp-minify-css'),
    rename = require("gulp-rename");

var appSourcesDir = 'src/main/resources/static/app';
var appStylesDir = 'src/main/resources/static/styles';

var buildJsDir = 'build/resources/main/static/app';
var buildCssDir = 'build/resources/main/static/css';

// configure the jshint task
gulp.task('jshint', function() {
    return gulp.src(appSourcesDir + '/**/*.js')
        .pipe(jshint())
        .pipe(jshint.reporter('jshint-stylish'))
        .pipe(jshint.reporter('fail'));
});

// Task to watch resource changes
gulp.task('watch', ['watch-styles', 'watch-js']);

// Task to watch style changes
gulp.task('watch-styles', function() {
    gulp.watch(appStylesDir + '/**/*.less' , ['build-css']);
});

// Task to watch js changes
gulp.task('watch-js', function() {
    gulp.watch(appSourcesDir + '/**/*.js', ['jshint']);
});

// Task to build all frontend artifacts
gulp.task('build', ['build-js', 'build-css']);

// Task to build js file
gulp.task('build-js', function() {
    return gulp.src(appSourcesDir + '/**/*.js')
        .pipe(sourcemaps.init())
        .pipe(concat('home.js'))
        // only uglify if gulp is ran with '--type prod
        .pipe(gutil.env.type === 'prod' ? uglify() : gutil.noop())
        .pipe(sourcemaps.write())
        .pipe(gulp.dest(buildJsDir));
});

// Task to build css file
gulp.task('build-css', function() {
    gulp.src(appStylesDir + '/main.less')
        .pipe(less())
        .pipe(gutil.env.type === 'prod' ? minifyCSS() : gutil.noop())
        .pipe(rename('home.css'))
        .pipe(gulp.dest(buildCssDir));
});