#![cfg_attr(not(target_os = "android"), allow(unused_imports, unused_variables))]

use android_logger::{Config, FilterBuilder};
use log::{LevelFilter, error, trace};

slint::include_modules!();

#[cfg(target_os = "android")]
#[unsafe(no_mangle)]
fn android_main(app: slint::android::AndroidApp) {
    android_logger::init_once(
        Config::default()
            .with_max_level(LevelFilter::Trace)
            .with_tag("mytag")
            .with_filter(
                FilterBuilder::new()
                    .parse("debug,hello::crate=error")
                    .build(),
            ),
    );

    trace!("this is a verbose {}", "message");
    error!("this is printed by default");

    // ── Critical fix: initialise the Android platform BEFORE any Slint calls ──
    slint::android::init(app).expect("Slint Android platform initialisation failed");

    let ui = AppWindow::new().expect("Failed to create AppWindow");
    ui.on_request_increase_value({
        let ui_handle = ui.as_weak();
        move || {
            let ui = ui_handle.unwrap();
            ui.set_counter(ui.get_counter() + 1);
        }
    });

    let result = ui.run();
    match result {
        Ok(_) => log::info!("Slint app exited successfully"),
        Err(_) => log::error!("Slint app failed to run"),
    }
}
