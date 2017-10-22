var gulp = require('gulp'),
    jshint = require('gulp-jshint'),
    sourcemaps = require('gulp-sourcemaps'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    gutil = require('gulp-util'),
    less = require('gulp-less'),
    cleanCSS  = require('gulp-clean-css'),
    rename = require("gulp-rename"),
    templateCache = require('gulp-angular-templatecache'),
    htmlmin = require('gulp-htmlmin'),
    runSequence = require('run-sequence');

var appSourcesRoot = 'src/main/resources/static';
var appJsDir = appSourcesRoot + '/app';
var appStylesDir = appSourcesRoot + '/styles';

var buildDir = gutil.env.buildtype === 'prod' ? 'build/resources/main/static' : 'out/production/resources/static';
var buildJsDir = buildDir + '/app';
var buildCssDir = buildDir + '/css';

// configure the jshint task
gulp.task('jshint', function() {
    return gulp.src(appJsDir + '/**/*.js')
        .pipe(jshint())
        .pipe(jshint.reporter('jshint-stylish'))
        .pipe(jshint.reporter('fail'));
});

// Task to watch resource changes. If a resource changes, the build is triggered
gulp.task('watch', function() {
    gulp.watch(appSourcesRoot + '/**/*.*', ['build']);
});

gulp.task('build', function(callback) {
    runSequence('jshint',
                'build-templatecache',
                'build-js',
                'build-css',
        callback);
});

// Task to build js file
gulp.task('build-js', function() {
    return gulp.src([appJsDir + '/**/*.js', buildDir + '/generated/**/*.js'])
        .pipe(gutil.env.buildtype !== 'prod' ? sourcemaps.init() : gutil.noop())
        .pipe(concat('home-min.js'))
        .pipe(uglify())
        .pipe(gutil.env.buildtype !== 'prod' ? sourcemaps.write() : gutil.noop())
        .pipe(gulp.dest(buildJsDir));
});

gulp.task('build-templatecache', function () {
    return gulp.src(appSourcesRoot + '/**/*.html')
        .pipe(htmlmin({
                        collapseWhitespace: true,
                        removeComments: true,
                        collapseBooleanAttributes: true,
                        removeAttributeQuotes: true,
                        removeRedundantAttributes: true,
                        removeEmptyAttributes: true
                       }))
        .pipe(templateCache('templatecache.js'))
        .pipe(gulp.dest(buildDir + '/generated'));
});

// Task to build css file
gulp.task('build-css', function() {
    return gulp.src(appStylesDir + '/main.less')
        .pipe(less())
        .pipe(cleanCSS())
        .pipe(rename('home.css'))
        .pipe(gulp.dest(buildCssDir));
});